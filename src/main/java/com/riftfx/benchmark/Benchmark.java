package com.riftfx.benchmark;

import com.riftfx.interpreter.JavaFXRenderer;
import com.riftfx.interpreter.UIRenderer;
import com.riftfx.interpreter.Interpreter;
import com.riftfx.resolution.Resolver;
import com.riftfx.ast.Stmt;
import com.riftfx.error.ErrorReporter;
import com.riftfx.parser.Parser;
import com.riftfx.scanner.Scanner;
import com.riftfx.scanner.Token;

import javafx.application.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark harness for measuring RiftFX performance metrics:
 *   1. Startup time (process launch to first rendered frame)
 *   2. Memory footprint (heap usage after first render)
 *   3. Full pipeline time (file read to AST to resolve to interpret)
 *
 * Usage:
 *   java -cp <classpath> com.riftfx.benchmark.Benchmark examples/simple_counter.rfx
 *
 * Runs 10 iterations (plus 1 warm-up) and reports the median.
 */
public class Benchmark {

    private static final int WARMUP_RUNS = 1;
    private static final int MEASURED_RUNS = 10;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: Benchmark <path-to-rfx-file>");
            System.exit(1);
        }

        String path = args[0];

        // Verify file exists
        if (!Files.exists(Paths.get(path))) {
            System.err.println("File not found: " + path);
            System.exit(1);
        }

        // Initialize JavaFX toolkit (required before any UI operations)
        CountDownLatch fxReady = new CountDownLatch(1);
        Platform.startup(fxReady::countDown);
        fxReady.await(5, TimeUnit.SECONDS);

        System.out.println("=== RiftFX Benchmark ===");
        System.out.println("Script: " + path);
        System.out.println("Warm-up runs: " + WARMUP_RUNS);
        System.out.println("Measured runs: " + MEASURED_RUNS);
        System.out.println();

        // --- Benchmark 1: Pipeline Execution Time ---
        List<Long> pipelineTimes = new ArrayList<>();

        // Warm-up
        for (int i = 0; i < WARMUP_RUNS; i++) {
            runPipeline(path);
        }

        // Measured runs
        for (int i = 0; i < MEASURED_RUNS; i++) {
            // Reset interpreter state cache between runs
            Interpreter.stateCache.clear();
            Interpreter.currentStateIndex = 0;

            long startNs = System.nanoTime();
            runPipeline(path);
            long endNs = System.nanoTime();

            pipelineTimes.add(endNs - startNs);

            // Small delay between runs to let GC settle
            Thread.sleep(100);
        }

        // --- Benchmark 2: Memory Footprint ---
        Interpreter.stateCache.clear();
        Interpreter.currentStateIndex = 0;

        System.gc();
        Thread.sleep(500);
        System.gc();
        Thread.sleep(200);

        long memBefore = usedHeapBytes();

        runPipeline(path);

        // Wait for JavaFX to settle
        CountDownLatch renderDone = new CountDownLatch(1);
        Platform.runLater(renderDone::countDown);
        renderDone.await(5, TimeUnit.SECONDS);
        Thread.sleep(1000);

        long memAfter = usedHeapBytes();
        long memDelta = memAfter - memBefore;

        // --- Benchmark 3: Full Startup Time (includes file I/O) ---
        List<Long> startupTimes = new ArrayList<>();
        for (int i = 0; i < MEASURED_RUNS; i++) {
            Interpreter.stateCache.clear();
            Interpreter.currentStateIndex = 0;

            long startNs = System.nanoTime();
            runFullStartup(path);
            long endNs = System.nanoTime();

            startupTimes.add(endNs - startNs);
            Thread.sleep(100);
        }

        // --- Report Results ---
        long medianPipeline = median(pipelineTimes);
        long medianStartup = median(startupTimes);

        System.out.println("=== Results (median of " + MEASURED_RUNS + " runs) ===");
        System.out.println();
        System.out.printf("  Pipeline time:    %,d ms  (scan + parse + resolve + interpret)%n",
                TimeUnit.NANOSECONDS.toMillis(medianPipeline));
        System.out.printf("  Startup time:     %,d ms  (file I/O + pipeline + render)%n",
                TimeUnit.NANOSECONDS.toMillis(medianStartup));
        System.out.printf("  Heap delta:       %.1f MB  (heap used after render - heap before)%n",
                memDelta / (1024.0 * 1024.0));
        System.out.printf("  Heap total:       %.1f MB  (total heap used after render)%n",
                memAfter / (1024.0 * 1024.0));
        System.out.println();

        System.out.println("--- All pipeline times (ms) ---");
        for (int i = 0; i < pipelineTimes.size(); i++) {
            System.out.printf("  Run %2d: %,d ms%n", i + 1,
                    TimeUnit.NANOSECONDS.toMillis(pipelineTimes.get(i)));
        }

        System.out.println();
        System.out.println("--- All startup times (ms) ---");
        for (int i = 0; i < startupTimes.size(); i++) {
            System.out.printf("  Run %2d: %,d ms%n", i + 1,
                    TimeUnit.NANOSECONDS.toMillis(startupTimes.get(i)));
        }

        System.out.println();
        System.out.println("=== Copy these into the paper (Table IV) ===");
        System.out.printf("  Startup (ms):  %d%n", TimeUnit.NANOSECONDS.toMillis(medianStartup));
        System.out.printf("  RSS (MB):      %.1f%n", memAfter / (1024.0 * 1024.0));
        System.out.println("  Recompose (ms): [measure manually - see BENCHMARK_INSTRUCTIONS.md]");

        Thread.sleep(2000);
        Platform.exit();
    }

    /**
     * Runs the full compilation pipeline (scan, parse, resolve, interpret).
     * Interpretation is scheduled on the JavaFX thread and waited on.
     */
    private static void runPipeline(String path) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String sourceCode = new String(bytes, StandardCharsets.UTF_8);

        ErrorReporter errorReporter = new ErrorReporter();

        Scanner scanner = new Scanner(sourceCode, errorReporter);
        List<Token> tokens = scanner.scan();
        if (errorReporter.hadError()) throw new RuntimeException("Scanner error");

        Parser parser = new Parser(tokens, errorReporter);
        List<Stmt> statements = parser.parse();
        if (errorReporter.hadError()) throw new RuntimeException("Parser error");

        Resolver resolver = new Resolver(errorReporter);
        resolver.resolve(statements);
        if (errorReporter.hadError()) throw new RuntimeException("Resolver error");

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                UIRenderer renderer = new JavaFXRenderer();
                Interpreter interpreter = new Interpreter(statements, errorReporter, renderer);
                interpreter.interpret();
            } catch (Exception e) {
                System.err.println("Runtime error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    private static void runFullStartup(String path) throws Exception {
        runPipeline(path);
    }

    private static long usedHeapBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private static long median(List<Long> values) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        if (n % 2 == 0) {
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2;
        } else {
            return sorted.get(n / 2);
        }
    }
}

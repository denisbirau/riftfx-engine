# **RiftFX Architecture Documentation**

## **Scanner Layer (com.riftfx.scanner)**

The Scanner (or Lexical Analyzer) is the foundational layer of the compiler/interpreter. Its primary responsibility is to read the raw source code string character by character and convert it into a meaningful sequence of Token objects. This process groups raw text into the foundational vocabulary of the RiftFX language.

### **1\. TokenType (Enum)**

The TokenType enum defines the complete vocabulary of the language. It categorizes tokens into distinct groups to be easily consumed by the Parser later:

* **Single-Character Tokens:** Operators and delimiters like \+, \-, \*, /, %, (, ), {, }, \[, \], ;, ,, ., ?, :.  
* **One or Two-Character Tokens:** Relational and logical operators, as well as arrows: \=, \==, \!, \!=, \<, \<=, \>, \>=, \-\>.  
* **Literals:** Variable user inputs like STRING, NUMBER, and IDENTIFIER.  
* **Keywords:** Reserved words in the language.  
* **Control Flow:** if, else, while, for, break, return.  
* **Logical/Type:** true, false, null, and, or.  
* **Declarations:** let (variables), def (functions), class (objects).  
* **OOP:** this, extends, super.  
* **Built-ins:** print.  
* **End of File:** EOF acts as a sentinel value to signal the end of the source code.

### **2\. Token (Record)**

The Token is implemented as a concise, immutable Java record. It acts as a data carrier holding:

* **type:** The classification of the token (from TokenType).  
* **lexeme:** The exact string of characters from the source code that formed this token.  
* **line:** The line number where the token was found, crucial for generating accurate error messages.

### **3\. Scanner (Class)**

The Scanner class houses the core lexical logic. It iterates through the sourceCode string using a two-pointer approach (startIndex and currentIndex) to isolate individual lexemes.

**Key Features & Implementation Details:**

* **Whitespace and Newline Handling:** Spaces, tabs, and carriage returns are safely ignored, while newline characters increment the internal line counter to keep error reporting accurate.  
* **Comment Ignorance:**  
  * *Single-line (//):* Skips all characters until a newline is encountered.  
  * *Multi-line (/\* ... \*/):* Consumes characters across multiple lines until the closing \*/ is found. It tracks internal newlines to keep the line count accurate and reports an error if the file ends before the comment is closed.  
* **Advanced String Interpolation:** The scanner supports dynamic string interpolation (e.g., "Value: ${x}"). It implements this by "desugaring" the interpolation at the lexical level. When ${ is encountered inside a string, it closes the string token, emits a \+ and a (, and uses a Stack\<Integer\> (interpolationDepths) alongside a brace depth counter to track nested structures. When the corresponding closing } is found, it emits a ) and a \+, and then seamlessly resumes scanning the rest of the string literal.  
* **Escape Sequences:** Properly handles standard escape sequences within strings (e.g., \\", \\\\, \\n, \\t, \\b, \\r, \\f) ensuring they are treated as part of the string content rather than early terminators.  
* **Maximal Munch Principle:** For operators that can be one or two characters (like \= vs \==), the scanner looks ahead (nextCharacter()) to match the longest possible token, resolving ambiguities.  
* **Keyword vs. Identifier Resolution:** When an alphanumeric string is scanned, the scanner checks it against a predefined KEYWORDS map. If a match is found, it is emitted as that specific keyword token (e.g., TokenType.LET); otherwise, it defaults to a standard TokenType.IDENTIFIER.  
* **Error Reporting:** Tightly integrated with an external ErrorReporter. If the scanner encounters an unrecognized character or an unterminated string/comment, it gracefully reports the error with the exact line number without immediately crashing, allowing multiple lexical errors to be caught in a single pass.

## ---

**AST Layer (com.riftfx.ast)**

The Abstract Syntax Tree (AST) layer defines the structural representation of the RiftFX source code. After the Scanner produces a flat list of tokens, the Parser will organize them into this tree structure based on the language's grammar.

A standout feature of this layer is its utilization of modern Java features—specifically sealed interfaces and records. This design choice provides an extremely concise, immutable, and type-safe way to define the AST nodes. It eliminates traditional boilerplate code (like explicit constructors, getters, and Visitor pattern interfaces) and pairs perfectly with Java's pattern matching (switch expressions) for later interpreter and resolution stages.

The AST is divided into two primary categories: **Expressions** (code that evaluates to a value) and **Statements** (code that performs an action).

### **1\. Expressions (Expr)**

An expression always produces a value. The Expr sealed interface strictly permits the following node types:

**Core Expressions:**

* **Literal:** Represents raw values like numbers, strings, booleans, or null (e.g., 42, "Hello", true).  
* **Unary:** Prefix operations, such as negation or logical NOT (e.g., \-x, \!isValid).  
* **Binary:** Infix operations containing a left and right expression (e.g., a \+ b, x \== y).  
* **Ternary:** Conditional expressions (e.g., condition ? trueExpr : falseExpr).  
* **Group:** Explicit groupings using parentheses to override standard operator precedence (e.g., (a \+ b) \* c).

**Variables & Scope:**

* **Lookup:** Represents fetching a variable's value by its identifier. It includes a Resolution object, which is populated during the semantic analysis phase to determine exactly which environment scope the variable lives in.  
* **Assignment:** Assigning a new value to an existing identifier (e.g., x \= 10). Like Lookup, it utilizes a Resolution object for precise scope tracking.

**Functions & Methods:**

* **Call:** Represents a function or method invocation. Notably, it takes a list of Argument records, which support named arguments (indicated by nameToken), allowing for flexible function calls.  
* **Lambda:** Anonymous functions (closures) defined inline, containing a list of parameter tokens and a body of statements.

**Object-Oriented Programming (OOP):**

* **GetMember / SetMember:** Accessing or modifying properties on an object (e.g., user.name or user.name \= "Alice").  
* **This:** Refers to the current object instance within a class method. Tracks its scope via Resolution.  
* **Super:** Refers to a method on the parent class. Also utilizes Resolution.

**Data Structures:**

* **ArrayDefinition:** Array literals (e.g., \[1, 2, 3\]).  
* **SubscriptGet / SubscriptSet:** Accessing or modifying elements within an array or dictionary using bracket notation (e.g., list\[0\], map\["key"\] \= value).  
* **ObjectLiteral:** Inline object/dictionary definitions mapping Property names to values (e.g., { x: 10, y: 20 }).

### **2\. Statements (Stmt)**

Statements form the backbone of the program's control flow and state manipulation. They do not evaluate to values themselves. The Stmt sealed interface includes:

**Declarations:**

* **Let:** Variable declaration and initialization (e.g., let x \= 5;).  
* **Def:** Function or method declaration, storing its name, parameters, and block body.  
* **Class:** Class definition, wrapping its name, a list of Def methods, and an optional superclassLookupExpression for inheritance.

**Control Flow:**

* **If:** Conditional branching, housing the condition expression, the "then" branch, and an optional "else" branch.  
* **While:** A standard loop executing as long as the condition evaluates to true.  
* **Break:** Terminates the nearest enclosing loop.  
* **Return:** Exits the current function, optionally returning an evaluated Expr back to the caller.

**Utility & Grouping:**

* **Expression:** An expression wrapper that allows an expression to act as a standalone statement (useful for executing function calls where the return value is ignored, like doWork();).  
* **Print:** A built-in statement for outputting values to the console/standard out.  
* **Block:** A collection of statements wrapped in curly braces { ... }, creating a new local scope for variables defined within it.

## ---

**Parser Layer (com.riftfx.parser)**

The Parser layer is responsible for taking the flat list of Token objects generated by the Scanner and organizing them into the structured Abstract Syntax Tree (AST). It checks the tokens against the grammatical rules of the RiftFX language.

To manage complexity, this layer is split into three main components: a cursor utility (TokenStream), a recursive descent parser for statements (Parser), and a Pratt parser for expressions (ExpressionParser).

### **1\. TokenStream**

The TokenStream class acts as a stateful cursor that navigates through the list of tokens. It encapsulates the bounds-checking and iteration logic, providing a clean API for the parsers to consume tokens.

* **Lookahead & Inspection:** Methods like current(), previous(), check(), and peek(offset) allow the parser to inspect tokens without consuming them.  
* **Consumption:**  
  * advance() moves the cursor forward unconditionally.  
  * match(TokenType...) consumes the current token if it matches any of the provided types, returning true on a successful match.  
  * consume(TokenType, errorMessage) strictly enforces the grammar. If the current token matches the expected type, it advances; otherwise, it immediately throws a ParseError to initiate error recovery.

### **2\. Parser (Statements and Declarations)**

The Parser class handles the outer structure of the program using a Recursive Descent parsing technique. It starts from the top (declarations) and works its way down to specific statements.

* **Declarations vs. Statements:** The parsing loop distinguishes between variable/function/class declarations (parseDeclaration()) and standard control-flow or side-effect statements (parseStatement()).  
* **Error Recovery (Panic Mode):** If a syntax error is encountered (ParseError), the parser catches it, reports it via the ErrorReporter, and calls skipToNextStatement(). This method advances the token stream until it finds a statement boundary (like a ; or a keyword like let, if, while), allowing the parser to resynchronize and report multiple errors in a single pass rather than crashing on the first typo.  
* **Desugaring:** The for loop implementation (parseForStatement()) is a great example of syntactic sugar. Instead of creating a dedicated Stmt.For node, the parser actively translates (desugars) the for(init; condition; increment) loop into a Stmt.Block containing the initializer and a Stmt.While loop, simplifying the AST and the eventual interpreter.

### **3\. ExpressionParser (Pratt Parser)**

Because standard recursive descent struggles with operator precedence and left-associativity (leading to deeply nested, hard-to-read methods), expressions are handled by the ExpressionParser using a Top-Down Operator Precedence (Pratt) Parsing architecture.

**How It Works:**

* **Precedence Enum:** Defines the exact binding power of operators, from lowest (NONE) to highest (PRIMARY).  
* **Parse Rules:** An EnumMap binds every TokenType to a ParseRule. A rule contains:  
  * PrefixParseFn: How to parse the token if it appears at the start of an expression (e.g., \- as negation, number, (, {).  
  * InfixParseFn: How to parse the token if it appears in the middle of an expression (e.g., \+, \-, \*, . member access).  
  * Precedence: The binding strength of the operator.  
* **parsePrecedence(Precedence):** The core engine. It parses a prefix expression, then continually consumes infix operators as long as their precedence is higher than or equal to the current precedence context.

**Advanced Language Features Handled Here:**

* **Ternary Operator (?:):** Processed as right-associative (so it doesn't artificially bump precedence).  
* **Complex Assignments:** Safely casts left-hand expressions into valid assignment targets (Lookup to Assignment, GetMember to SetMember, SubscriptGet to SubscriptSet).  
* **Named Arguments:** Inside function calls (parseCallExpression), it checks if an identifier is immediately followed by an \= sign, packing it into an Expr.Argument with a designated name.  
* **Trailing Lambdas:** Inspired by languages like Kotlin, RiftFX allows lambda blocks to be passed outside the parentheses of a function call. If a { follows a call (or replaces parentheses entirely via parseOmittedParenthesesCall), it reads an optional parameter list separated by an \-\> arrow before parsing the block body.  
* **Object and Array Literals:** Supports concise instantiations like \[1, 2, 3\] or { key: value }.

## ---

**Resolution Layer (com.riftfx.resolution)**

The Resolution Layer (or Semantic Analyzer) acts as a bridge between the Parser and the Interpreter. While the Parser ensures the code is syntactically correct, the Resolver ensures it is semantically sound. Its primary job is to traverse the AST, enforce static scoping rules, and bind variables to their correct lexical environments so the Interpreter doesn't have to guess at runtime.

### **1\. Resolution (Class)**

A simple, mutable data container attached to specific AST nodes (like Lookup, Assignment, This, and Super). It holds an Integer distance.

* **Purpose:** The distance represents how many environment "hops" (or scopes) the Interpreter must ascend to find the exact variable being referenced. By calculating this statically ahead of time, RiftFX completely avoids the "dynamic scoping" bug (where closures might capture the wrong variable if a local variable shares the same name as an outer one).

### **2\. Resolver (Class)**

The Resolver performs a full, recursive traversal of the AST without actually executing any of the code. It acts similarly to an Interpreter, but instead of manipulating real values, it manipulates scope metadata.

**Key Features & Implementation Details:**

* **Lexical Scope Tracking (Stack\<Map\<String, Boolean\>\>):** The resolver uses a stack of maps to represent nested blocks of code.  
  * The map keys are the variable names (lexemes).  
  * The Boolean value tracks whether a variable is fully defined or just declared. This cleverly prevents temporal dead zone errors, such as initializing a variable with itself (let a \= a;).  
* **Distance Calculation (resolveLocal):** When the resolver encounters a variable usage (Expr.Lookup, Expr.Assignment, Expr.This, or Expr.Super), it searches outward through the scope stack. When it finds the matching declaration, it records the exact distance (e.g., 0 for the current local scope, 1 for the immediate outer block) and saves it into the node's Resolution object.  
* **Static Safety Checks:** Because the Resolver contextually tracks where it is in the code, it catches many semantic errors that the grammatical Parser cannot:  
  * *Loop Context (insideLoop):* Throws an error if a break statement is used outside of a while or for loop.  
  * *Function Context (currentFunction):* Ensures return statements are only used inside functions. It also specifically prevents return \<value\>; inside class constructors (INITIALIZER), as constructors implicitly return the instance.  
  * *Class Context (currentClass):* Validates that this and super are only used inside class methods. It also ensures a class isn't attempting to inherit from itself.  
* **Implicit Variable Injection (OOP):** When analyzing classes and subclasses, the resolver artificially injects this and super keywords into the scope stack. This seamlessly treats them as standard scoped variables, allowing methods and nested closures to capture this naturally without complex runtime logic.  
* **Two-Pass Function Resolution:** When resolving a def function, the resolver declares and defines the function's name in the current scope before resolving its body. This elegantly enables recursive function calls, as the function's body will successfully find its own name in the surrounding scope.

## ---

**Interpreter Layer (com.riftfx.interpreter)**

The Interpreter layer is the beating heart of the RiftFX engine. It takes the parsed and semantically resolved AST and brings it to life. By traversing the tree, it evaluates expressions, executes statements, manages memory, and handles side effects (like printing to the console or rendering UI elements). This layer implements a Tree-Walk Interpreter leveraging modern Java pattern matching to cleanly separate the execution logic for every node type.

### **1\. The Core Engine (Interpreter)**

The Interpreter class orchestrates the execution of the program. It relies heavily on two primary methods: execute() for statements (which produce side effects) and evaluate() for expressions (which compute to a value).

**Key Capabilities:**

* **Dynamic Typing & Evaluation:** Evaluates literals, performs arithmetic/logical operations, and cleanly maps RiftFX types to underlying Java types (e.g., numbers to Double, text to String, arrays to NativeArray, booleans to Boolean).  
* **Advanced Argument Resolution:** The resolveArguments method is a sophisticated piece of logic that handles:  
  * *Positional Arguments:* Standard sequential arguments.  
  * *Named Arguments:* Maps arguments explicitly to parameter names, throwing errors on collisions.  
  * *Trailing Lambdas:* Inspired by Kotlin, if a lambda is passed outside the parentheses of a function call, the interpreter automatically maps it to the final parameter of that function.  
* **String Built-ins:** Natively interprets common string operations directly on string literals or variables (e.g., str.len, str.toUpperCase, str.toLowerCase).  
* **State & UI Context:** Maintains the globalEnvironment, tracks the currentEnvironment as scopes shift, and holds a reference to the UIRenderer to allow RiftFX scripts to draw to the screen.

### **2\. Memory & Lexical Scoping (Environment)**

The Environment class represents a lexical scope—a map connecting variable names to their current values.

* **Scope Chaining:** Environments hold a reference to their enclosingEnvironment, creating a linked list of nested scopes (global \-\> function \-\> block).  
* **Static Resolution Integration:** Notice the getAt and updateAt methods. Instead of traversing the scope chain by guessing at runtime, it uses the exact distance calculated by the Resolver layer. It simply jumps exactly *n* steps up the chain (ancestor(distance)) to find the variable in $O(1)$ time relative to the scope depth. This guarantees complete lexical scoping safety.

### **3\. Functions & Callables (Callable, Function)**

Functions are treated as first-class citizens in RiftFX, meaning they are evaluated as objects and can be passed around.

* **Callable Interface:** A contract for anything that can be invoked like a function. It defines the arity() (expected number of arguments) and the exact call() logic.  
* **Function Class:** Represents a user-defined function, method, or lambda.  
  * *Closures:* When a Function is created, it captures the currentEnvironment at that exact moment. This allows the function to "remember" the variables that were in scope when it was declared, forming a perfect closure.  
  * *Method Binding:* The bindInstance() method dynamically creates a new environment just for the method call, injecting the this keyword so it points to the calling instance.  
* **Control Flow via Exceptions (Return, Break):** Because Java's call stack gets deep during recursive AST evaluation, returning from a RiftFX function or breaking from a loop is elegantly handled by throwing custom RuntimeExceptions. When executeReturnStatement fires, it throws a Return object containing the value. The call() method catches this exception, extracts the value, and cleanly exits the function.

### **4\. Object-Oriented Programming (Class, Instance)**

RiftFX natively supports OOP through a standard class-based approach.

* **Class:** Implements Callable. When you "call" a class (e.g., let obj \= MyClass();), it creates a new Instance, looks up the constructor method, binds the new instance to this, and executes the constructor block. It also stores a reference to its superclass to handle method inheritance.  
* **Instance:** Holds the state (fields) of an object in a members map. When a property is accessed (get), it checks fields first, and if not found, it asks its Class for a method, returning a bound function.

### **5\. UI Rendering Engine (UIRenderer, JavaFXRenderer)**

This is the "FX" in RiftFX. This layer seamlessly bridges the interpreted language with native Java UI frameworks.

* **UIRenderer Interface:** An abstraction defining how the language engine interacts with a visual frontend (pushing containers, popping them, and adding components).  
* **JavaFXRenderer:** The concrete implementation using a Stack\<Pane\> (uiContext). As RiftFX code conceptually builds nested UI elements, this renderer pushes JavaFX Pane objects to the stack, adds native Node components to the top container, and pops them when blocks close. This turns RiftFX into a declarative UI scripting language out of the box.

## ---

**Standard Library Layer (com.riftfx.stdlib) \- Part 1**

The Standard Library (stdlib) layer provides the built-in global functions, objects, and data types that make RiftFX a fully functional and capable language out of the box. It acts as the bridge between interpreted RiftFX scripts and the underlying native Java ecosystem.

### **1\. Core Interop Architecture (com.riftfx.stdlib.core)**

This subpackage provides the infrastructure necessary to inject Java code into the RiftFX Interpreter safely and cleanly.

* **NativeObject (Interface):** The fundamental contract for any Java class that wants to act like a RiftFX object. It requires implementing getMember and setMember, allowing RiftFX scripts to use standard dot-notation (e.g., object.property) to interact with Java methods or fields.  
* **AbstractCallable (Class):** An abstract implementation of the Callable interface that dramatically reduces boilerplate. It handles arity checking (minimum and maximum argument counts) and parameter naming, letting you focus purely on the execution logic in the call() method.  
* **InterpreterUtils (Class):** A safety wrapper for native calls. It provides getArgument() to safely cast RiftFX arguments to Java types (throwing descriptive errors if types mismatch) and executeSafe() to catch Java exceptions and route them through the RiftFX ErrorReporter so the interpreter doesn't crash abruptly.  
* **StandardLibrary (Class):** The master registry. It contains a static GLOBALS map that binds string names to their respective native implementations. When the Interpreter boots up, it injects everything in this map (like Math, File, Window, etc.) directly into the global environment.

### **2\. Global Modules**

These modules are singleton objects available globally in any RiftFX script.

**App (com.riftfx.stdlib.system.NativeApp)**

Provides application-level lifecycle controls.

* App.exit(code): Safely shuts down the JavaFX platform and exits the system with the given status code.

**File (com.riftfx.stdlib.io.NativeFileIO)**

A synchronous file system API for interacting with the local machine.

* File.readText(path): Reads the entire contents of a file into a string.  
* File.writeText(path, content): Writes string content to a file, creating it if it doesn't exist or overwriting it if it does.  
* File.exists(path): Returns a boolean indicating if a file/directory exists.  
* File.delete(path): Deletes a file.  
* File.listDirectory(path): Returns an array of file names contained within the specified directory.

**Math (com.riftfx.stdlib.math.NativeMath)**

Provides standard mathematical constants and utility functions.

* **Constants:** Math.PI, Math.E.  
* **Methods:**  
  * Math.sqrt(value): Returns the square root.  
  * Math.random(): Generates a random double between 0.0 and 1.0.  
  * Math.floor(value): Rounds down to the nearest integer.  
  * Math.round(value, decimals): Rounds a number to a specific number of decimal places (defaults to 0).

### **3\. Native Data Types (com.riftfx.stdlib.types)**

These classes back the foundational data structures of the language.

**NativeArray**

The underlying Java representation of a RiftFX array literal (e.g., \[1, 2, 3\]). It wraps a List\<Object\>.

* **Properties:** array.len (returns the size of the array).  
* **Methods:**  
  * array.push(item): Appends an item to the end of the array.  
  * array.removeAt(index): Removes the item at the specified numeric index.  
  * array.indexOf(item): Returns the first index of the item, or \-1 if not found.

**NativeDate & NativeDateFactory**

Provides modern Date/Time manipulation (wrapping Java's LocalDate).

* **Factory (Date global):**  
  * Date.now(): Returns a new date instance representing today.  
  * Date.parse("YYYY-MM-DD"): Creates a date from a string.  
* **Instance Methods/Properties:**  
  * date.year, date.month, date.day: Numeric properties.  
  * date.toString(): Returns the standard ISO string format.  
  * date.format(pattern): Formats the date using Java DateTimeFormatter patterns.  
  * date.addDays(n): Returns a new date object shifted by n days (dates are immutable).  
  * date.daysUntil(otherDate): Calculates the difference in days between two dates.  
  * date.isBefore(otherDate): Returns true if the date occurs before the provided date.

**NativeDictionary**

The implementation backing RiftFX object literals (e.g., { key: "value" }). It acts as a lightweight wrapper around a Java Map\<String, Object\>, intercepting getMember and setMember calls to dynamically read and write to the map at runtime.

## ---

**Standard Library Layer (com.riftfx.stdlib) \- Part 2: The UI Framework**

This subpackage is the crown jewel of the RiftFX standard library. It provides a modern, declarative, and highly reactive UI framework built on top of JavaFX. Inspired by modern toolkits like Jetpack Compose, SwiftUI, and React, this layer allows developers to build complex desktop applications using a clean, nested syntax.

### **1\. Core Architecture & The Rendering Pipeline (com.riftfx.stdlib.ui.core)**

The UI framework relies on a hierarchical rendering pipeline managed by the JavaFXRenderer (from the Interpreter layer).

* **AbstractUIComponent:** The base class for all visual nodes. It automatically handles the registration of the component into the current active container (the parent).  
* **ScopedContext:** A thread-local stack utility used to cleanly manage hierarchical components that don't fit the standard Pane structure (like Menus and Tabs). It ensures nested components know exactly who their parent is.  
* **UITheme:** A centralized CSS definition class that provides RiftFX's "Flat by Default" minimalist design system. It uses a neutral Slate palette, crisp borders, and breathable spacing, intentionally avoiding hardcoded heavy gradients or baked-in shadows so developers can cleanly override styles.
* **RendererUtils**: The bridge that applies styles to native nodes. Instead of blindly concatenating CSS strings, it actively parses base theme rules into a map and cleanly merges user-defined ModifierInstance overrides on top, ensuring final CSS payloads are clean and predictable.

### **2\. Layouts (com.riftfx.stdlib.ui.layout)**

Layout components define the structure of the application. They take a trailing lambda containing their sub-components.

* **Window:** The root entry point of a RiftFX application. It manages the underlying JavaFX Stage and Scene.  
* **Column & Row:** Wrappers around VBox and HBox, arranging children vertically or horizontally with breathable default spacing.  
* **Stack:** Stacks elements on top of each other (z-axis).  
* **Grid & GridCell:** For tabular layouts. GridCell requires specific row and column coordinates.  
* **ScrollPane & Spacer:** Utility layouts for adding scrollbars to overflowing content and pushing elements apart dynamically.  
* **TitledPane:** A collapsible accordion-style grouping component.

### **3\. State Management & Reactivity (com.riftfx.stdlib.ui.state)**

RiftFX implements a robust reactive state system. It eliminates manual UI updates by bridging the interpreter's state with JavaFX's Property listeners.

* **State & CreateState:** Variables created via State(initialValue) are cached in the Interpreter's memory. When state.value is updated, it automatically triggers a UI recomposition.  
* **Observe:** The recomposition boundary. Any UI components placed inside an Observe(state) { ... } block will be automatically destroyed and re-rendered whenever that specific state changes.  
* **ReactiveBinding:** A hidden engine class that sets up bidirectional syncs. When a user types in a TextField, the State updates immediately. Conversely, if background logic updates the State, the TextField updates automatically on the screen.

### **4\. The Modifier System (com.riftfx.stdlib.ui.modifier)**

Instead of exposing raw JavaFX styling methods, RiftFX uses a fluent, immutable Modifier object.

* **ModifierInstance:** Provides chainable, immutable styling functions (e.g., Modifier.size(200).padding(10).background("#ff0000").shadow()). Because of the new map-based rendering pipeline, applying a modifier will cleanly overwrite the underlying base theme property (e.g., a custom shadow color will completely replace the default card shadow).  
* **Dynamic Capabilities & Batches:** Includes utility bundles like Modifier.card() to instantly apply structural foundations (white background, borders, neutral shadow) that can be further chained and overridden. Modifiers like shadow("rgba(...)") dynamically accept parameters for granular visual control.

### **5\. UI Controls & Data Entry (com.riftfx.stdlib.ui.controls)**

A comprehensive suite of interactive widgets safely bound to the reactive state system.

* **Text & Buttons:** Text (Labels) and Button (with onClick handlers).  
* **Inputs:** TextField, PasswordField, and TextArea for text entry.  
* **Toggles & Selectors:** Checkbox, RadioButton, ComboBox, and DatePicker.  
* **Ranges:** Slider and Spinner for numeric inputs.  
* **Indicators:** ProgressBar and ProgressIndicator for loading states.  
* **Advanced:** ListView takes an array (or a State containing an array) and a builder lambda, dynamically generating scrollable lists of custom UI cells. Image supports loading external URLs or local file paths with clipping support.

### **6\. Navigation (com.riftfx.stdlib.ui.navigation)**

For multi-view desktop applications, RiftFX provides deeply nested navigation structures leveraging ScopedContext.

* **Tabs:** TabPane acts as the container, while Tab defines individual pages.  
* **Menus:** Build native desktop menus using MenuBar, Menu (which supports nesting for sub-menus), and MenuItem for clickable actions.

### **7\. Dialogs & Graphics (com.riftfx.stdlib.ui.dialogs, com.riftfx.stdlib.ui.graphics)**

* **Dialogs:** ShowAlert blocks execution to show standard OS popups (Information, Error, Warning). ShowFileChooser opens a native file explorer to select files.  
* **Canvas:** Canvas provides a low-level drawing area. It passes a NativeGraphicsContext to its lambda block, exposing methods like fillRect, fillOval, clearRect, and setFill for custom rendering and game loops.

## ---

**Error Layer (com.riftfx.error)**

The Error layer is a small but critical component of the RiftFX engine. It provides a centralized, standardized way to handle, track, and report errors across all phases of the language's lifecycle—from raw source scanning down to UI rendering.

By utilizing custom exception types and a unified reporting utility, RiftFX ensures that developers get precise, actionable feedback (including exact line numbers and the specific characters that caused the issue) rather than cryptic Java stack traces.

### **1\. ErrorReporter**

The ErrorReporter acts as the global diagnostics manager for the compiler and interpreter. It is instantiated once and passed down through the Scanner, Parser, Resolver, and Interpreter.

* **Standardized Output:** It formats all errors consistently (e.g., Error\[line 10\] at 'x': Variable already defined.) and routes them directly to the standard error stream (System.err).  
* **State Tracking (hadError):** This is a crucial feature for the compilation pipeline. If the Scanner or Parser registers an error, the hadError flag is set to true. The main application runner can check this flag to safely halt execution before passing a malformed AST to the Interpreter, preventing catastrophic runtime crashes.  
* **Token Awareness:** The report methods are overloaded to accept raw line numbers or specific Token objects. When a Token is passed, it intelligently formats the message based on whether the error occurred at a specific word/symbol or at the end of the file (EOF).

### **2\. ParseError (Class)**

A custom RuntimeException utilized exclusively by the Parser.

* **Purpose:** When the Parser encounters a token that violates the grammar of RiftFX, it throws a ParseError.  
* **Error Recovery:** Because it is an exception, throwing it instantly unwinds the parser's deep recursive descent stack. The Parser catches this exception, logs it via the ErrorReporter, and enters "panic mode"—skipping tokens until it finds the next statement boundary (like a ;) so it can continue parsing and report multiple syntax errors in a single pass.  
* **Context:** It holds a reference to the offending Token to guarantee the error message points exactly to the user's typo.

### **3\. RuntimeError (Class)**

A custom RuntimeException utilized exclusively by the Interpreter and standard library.

* **Purpose:** Handles errors that are syntactically valid but logically impossible to execute at runtime. Examples include dividing by zero, calling a method on a null object, or passing the wrong data type to a standard library function (e.g., Math.sqrt("hello")).  
* **Safe Execution:** Like ParseError, it captures the exact Token responsible for the crash. In the context of the UI framework (e.g., inside an Observe block or an onClick lambda), the InterpreterUtils.executeSafe method catches these runtime errors and routes them to the ErrorReporter so a bad button click doesn't crash the entire desktop application.

## ---

**Application Layer (com.riftfx.app)**

The Application layer is the entry point and orchestrator of the entire RiftFX engine. It brings all the previously discussed layers—Scanner, Parser, Resolver, and Interpreter—together into a single, cohesive execution pipeline. Additionally, it provides the bridge to the JavaFX application thread and introduces modern developer experience (DX) features.

### **1\. The Entry Point (Main)**

The Main class serves as the command-line interface for the language. It expects exactly one argument: the file path to the RiftFX script (.rfx or similar) that you want to execute.

Upon startup, it immediately initializes the underlying JavaFX toolkit via Platform.startup(() \-\> {}), which is strictly required before any UI components can be instantiated.

### **2\. The Compilation Pipeline (compileAndRun)**

This method represents the complete lifecycle of a RiftFX script, executing the phases sequentially. A critical architectural feature here is the **fail-fast error handling**. After every single phase, the engine checks errorReporter.hadError(). If an error occurred, the pipeline gracefully halts, preventing malformed data from causing hard crashes in downstream phases.

**The pipeline flows as follows:**

1. **File I/O:** Reads the source file into a UTF-8 String.  
2. **Lexical Analysis:** The Scanner converts the raw string into Token objects.  
3. **Parsing:** The Parser consumes the tokens and constructs the Abstract Syntax Tree (List\<Stmt\>).  
4. **Semantic Analysis:** The Resolver performs a static pass over the AST to bind lexical scopes and calculate variable distances.  
5. **Execution (UI Thread):** Finally, the Interpreter is spun up. Crucially, the interpreter is executed inside Platform.runLater(...). Because RiftFX is deeply integrated with JavaFX, all UI rendering and state manipulation must happen on the main JavaFX Application Thread.

### **3\. Hot Reload Engine (startHotReloadWatcher)**

To provide a modern, highly productive developer experience, RiftFX includes a native Hot Reload system.

* **Daemon Watcher:** It spins up a background daemon thread utilizing Java's NIO WatchService to monitor the parent directory of the executed script.  
* **Live Recompilation:** When it detects an ENTRY\_MODIFY event (i.e., you saved the file in your IDE), it slightly debounces the event (using Thread.sleep(50) to prevent file-lock collisions) and triggers compileAndRun() again.  
* **State Preservation Synergy:** Because the Interpreter layer utilizes a static stateCache mapped to component indices, hot-reloading the script re-evaluates the UI layout without destroying the user's current reactive State values, allowing for instantaneous, stateful UI tweaking\!\</String,\>\</Map\<String,\>
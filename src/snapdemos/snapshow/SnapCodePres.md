
# Introducing SnapCode

## Compelling features:

- Modern Java IDE for education
- Runs in the browser
- Java REPL Support
- Brings back Java Desktop technologies

---

## Modern Java IDE for education (1)

### Some IDEs are too much (IntelliJ, Eclipse, NB)

- Like learning to fly with a 747
- Heavy download
- Confusing to get started
- Easy to get lost

---

## Modern Java IDE for education (2)

### Some IDEs are too little (BlueJ, Greenfoot, JPG)

- Like learning math with a slide rule
- No code completion, symbol highlight, symbol lookup
- No project file management
- Little Java Desktop support (or none)
- No significant improvement or new entries in years

---

## Modern Java IDE for education (3)

### SnapCode is just right!

- Simple UI
- Modern editor
- Runs in the browser
- Java REPL Support

---

## Modern Java IDE for education

### Modern editor
- Syntax coloring, symbol highlight
- Symbol lookup (shortcut-click, right-click)
    - Declarations, references, super/interface, source, docs
- As-you-type error checking
- Code completion
    - Context-sensitive, receiver class, method gen
- Paired char selection, smart indent

---

# Runs in the Browser

- No need to install
- Makes it easy to run anytime and anywhere
    - Desktop, laptop, chrome book, tablet, phone, car console
- Great for quick experiments and tests
- Great for sharing
    - Makes it great for support and training
    - Tools -> Create Web Link (menu item) creates instant link
        - Source file is embedded in URL via "LZString" encoding
        - Like JSFiddle for Java

---

## Java REPL Support

- Implicit classes and main
- Support for 'var' and optional statement terminator
- Great for learning, teaching and experimentation
- Smart console support (charting, drawing, 3D)
- Charting support

```
    var x = new double[] { 1, 2, 3, 4 };
    var y = DoubleStream.of(x).map(d -> d * d).toArray();
    var chart = chart(x, y);
    show(chart);
```

---

## Brings back Java Desktop

- Good conventional paradigm for apps
    - Like iOS / Android / Flutter
- Swing fully supported
- Introduces new Java UI kit for browser: SnapKit!

---

## SnapKit: Modern Java UI Kit (1)

### Swing is too little:

- Out of date, no active feature development
- No support for mixing app components and graphics
- No built-in support for transforms, shadows, effects, animation
- Built on legacy AWT (unnecessary classes in hierarchy)
    - E.g.: Component -> Container -> JComponent
- Layout Manager debacle
- Platform look-and-feel debacle (write-once-debug-everywhere)
- Not optimized for browser

---

## SnapKit: Modern Java UI Kit (2)

### JavaFX is too much:

- Large additional footprint
- Changed many conventions
    - Display list graphics, CSS styling, Observers
- Mostly written in C/C++ (70%+)
- Doesn't run in browser*

---

## SnapKit: Modern Java UI Kit (3)

### SnapKit is just right:

- Light weight, but provides essential features of Swing and JavaFX
- Runs on Swing, JavaFX and HTML DOM (easily portable, future-proof)
- Provides a rich View class
    - Layouts, serialization, painting, transforms, effects, animation, events
    - Serialize to/from 'snp' file easily separates view declaration from code
- Provides a ViewOwner class
    - Easily createUI(), initUI(), resetUI(), respondUI()
    - Supports "Universal accessors" to get/set values from any view
- Provides a "universal" ViewEvent class to simplify event handling

---

## SnapKit: Modern Java UI Kit (4)

### Runs on desktop and browser via adapter layer

- Desktop: Uses Swing, Java2D, Graphics2D,
    - BufferedImage, JOGL
- Browser: Uses WebAPIs, DOM, HTMLCanvasElement,
    - CanvasRenderingContext2D, HTMLImageElement, WebGL
- Optimizes native platform rendering speed
- Minimizes JRE download (~15mb)

---

## CheerpJ JVM makes SnapCode & SnapKit possible

- Complete OpenJDK JRE in browser (Java 8 now, Java 11 - 17 this year)
- WASM runtime + JavaScript JIT
- Simple html launch script

    ```
    cheerpjInit();
    cheerpjRunMain("snapcode.app.App", "SnapCode.jar:SnapKit.jar");
    ```
  
- Respectable size and speed
    - ~20mb download, ~5 seconds initial launch, ~2 seconds cached

---

## SnapKit: More interesting features

- Parse package: Makes parsing easy by separating grammar and code
- Graphics (gfx) package: Makes painting and collision detection easy
- Text package: Facilitates reading, displaying and editing large text and rich text
- Props package: Supports 'property' ivars to automate serialization, copy/paste and undo
- Gfx3d package: Provides an abstraction to easily generate and manipulate 3D geometry
- Web package: Provides an abstraction unifying URLs, files and sites for easy manipulation
- Dev tools: SnapKit apps can all show a 'dev tools' pane to graphically inspect and debug

---

## SnapCode: More interesting features

- Integrated UI Builder
- External library support
- Version Control support
- File diff support
- File search, symbol search support
- Block programming

---

## Integrated UI Builder

- Easy to generate UI
- Separates UI from control code

---

## Block Programming


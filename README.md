# Temporize

Temporize is a template engine for java that compiles textual templates into java 
source files, which can then be used from your code.

## Why + advantages

* Speed
* No IO during runtime (i.e. no blocking, IO is done only during the compilation step)
* IDE auto-completion support
 
## Downsides

* After changing templates the project must be recompiled
 
## Supported markup
 
* `{$placeholder}`: creates a setter to assign a value for this placeholder
* `{$placeholder|modifier1|modifier2}`: encodes the value with the given modifiers.
  Modifiers are String->String functions.
* `{for block}...{/for}`: creates a subclass from the content that can be 
 assigned 0-n times 
* `{for block:com.example.mytemplate}` or `{for block}{+import:com.example.mytemplate}{/*}`: 
 imports a different template here
* `{if condition}...{/if}`: creates a conditional
* `{if condition}...{else}...{/if}`: creates a conditional with an else block

## Example

Template `tpl/index/myTemplate.html`:

    <html>
    <head>
      <title>{title|html}</title>
    </head>
    <body>
      <h1>{headline|stripnl|html}</h1>
      {if showIntroduction}<div>{introduction|html}</div>{/if}
      {if point} 
      <ul>
      {*point}<li><a href="/{target|urlenc}">{text|html}</a></li>{/*}
      </ul>
      {/if}
      {+import:assoc/footer}
    </body>
    </html>
    
Compile this to the target src-gen directory with the "template" top level namespace 
and this creates a class `template/index/MyTemplate.java` with the fully qualified name 
`template.index.MyTemplate`.

The generated code might look something like this:

    package template.template.index; 
    
    class MyTemplate {
        private String title;
        private String headline;
        private boolean showIntroduction;
        private String introduction;
        private Collection<Point> pointList;
        private templates.assoc.Footer footer; 
        
        MyTemplate() {}
        
        public MyTemplate setTitle(String title) {...}
        ...
        public MyTemplate showIntroduction(boolean showIntroduction) {...}
        public MyTemplate setPointList(Collection<Point> pointList) {...}
        public MyTemplate setFooter(template.assoc.Footer footer) {...}
        ...
        public MyTemplate write(Writer writer) {...}
        ...
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder()
            sb.append("<html>\n    <head>\n      <title>");
            sb.append(TemporizeFn.html(this.headline));
            sb.append("</title>\n    </head>");
            ...
            if (this.showIntroduction) {
                sb.append("<div>");
                sb.append(TemporizeFn.html(this.introduction));
                sb.append("</div>");
            }
            ...
            sb.toString();
        }
        
        public static class Point {
            public Point() {}
            public Point setTarget(String target) {...}
            ...
        }
    }

The methods toString or write are used to execute the template and get the result. So
in your code you could do:

    List<Points> points = new ArrayList<Points>();
    points.add(new Point().setTarget("current news").setText("news"));
    points.add(new Point().setTarget("archives").setText("archives"));

    System.out.println(new MyTemplate()
        .setTitle("hello world")
        .setHeadline("welcome, stranger")
        .showIntroduction(true)
        .setIntroduction("stay a while and listen!")
        .setPointList(points)
        .setFooter(new Footer().setCopyright(String.valueOf(2017)));

## Notes

* Top level templates and fully qualified includes generate top level classes,
 while blocks create inner classes.
 
* There are some pre-defined modifiers but additional ones can be defined.

* No reflection at runtime.

## Benchmark

* TODO
* It's fast.
* Compilation may not as fast but that doesn't really matter because it happens at 
 project compile time anyway.
* Packaging and distributing the compiled templates is easy. There is no need to 
 package the original template sources.

## How to compile

* Call manually: `java -jar temporize.jar tpl/ src_gen/`
* Maven/Gradle Plugin (todo)

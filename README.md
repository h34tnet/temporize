# Temporize

Temporize is a template engine for java that turns textual templates into java class 
source files in a pre-compile step.

## Why + advantages

* Speed
* No IO during runtime (i.e. no blocking, IO is done soley during the compilation step)
* IDE auto-completion support
 
## Downsides

* After changing templates the project must be recompiled
 
## Supported markup
 
* `{{placeholder}}`: creates a setter to assign a value for this placeholder
* `{{placeholder|modifier1|modifier2}}`: encodes the value with the given modifiers
* `{{*block}}...{{/block}}`: creates a subclass from the content that can be 
 assigned 0-n times 
* `{{*block:com.example.mytemplate}}` or `{{*block}}{{+import:com.example.mytemplate}}{{/block}}`: 
 imports a different template here
* `{{if condition}}...{{/if}}`: creates a conditional

## Example

Template `tpl/index/myTemplate.html`:

    <html>
    <head>
      <title>{{title|html}}</title>
    </head>
    <body>
      <h1>{{headline|stripnl|html}}</h1>
      {{if showIntroduction}}<div>{{introduction|html}}</div>{{/if}}
      {{if point}} 
      <ul>
      {{*point}}<li><a href="/{{target|urlenc}}">{{text|html}}</a></li>{{/point}}
      </ul>
      {{/if}}
      {{+import:assoc/footer}}
    </body>
    </html>
    
Compile this to the target src-gen directory and this creates a 
class `template/index/MyTemplate.java` with the fully qualified name 
`template.index.MyTemplate`.

The class might look something like this:

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
        public MyTemplate setFooter(templates.assoc.Footer footer) {...}
        ...
        public MyTemplate write(Writer writer) {...}
        ...
        @Override
        public String toString() {...}
        
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
* Compilation is not as fast but that doesn't really matter because it happens at 
 project compile time anyway.
* Packaging and distributing the compiled templates is easy. There is no need to 
 package the original template sources.

## How to compile

* Call manually: `java -jar temporize.jar tpl/ src_gen/`
* As a pre-compilation step
* Gradle Plugin (todo)
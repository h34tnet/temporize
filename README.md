# Temporize

Temporize is a template engine for java that compiles textual templates into java 
source files, which can then be used from your code.

## Why + advantages

* Speed - it's ~30% faster than Rocker at the https://github.com/mbosecke/template-benchmark 
  Stock benchmark
* No file IO during runtime (i.e. no blocking, IO is done only during the compilation step)
  (except for the JVMs startup itself)
* IDE auto-completion support for all IDEs without any extra plug-ins
* Packaging and distributing the compiled templates is easy. There is no need to 
  package the original template sources.
* Bytecode obfuscators work on the templates.

## Downsides

* After changing templates the project must be recompiled
 
## Supported markup
 
* `{$placeholder}`: creates a setter to assign a value for this placeholder
* `{$placeholder|modifier1|modifier2}`: setter that also applies the given modifiers to the value
  Modifiers are String->String functions defined in a special class
* `{for $block}...{/for}`: creates a subclass from the content that can be
 assigned 0-n times 
* `{include com.example.mytemplate as $mytemplate}`: imports a different template here
  note that the import gets its own top level class and can be re-used in different templates
* `{if $condition}...{/if}`: creates a conditional
* `{if $condition}...{else}...{/if}`: creates a conditional with an alternative

## Example

Template `tpl/index/MyTemplate.html`:

    <html>
    <head>
      <title>{$title|html}</title>
    </head>
    <body>
      <h1>{$headline|stripnl|html}</h1>
      {if $showIntroduction}<div>{$introduction|html}</div>{/if}
      {if $point} 
      <ul>
      {for $point}<li><a href="/{$target|urlenc}">{$text|ellipsize80|html}</a></li>{/for}
      </ul>
      {else}
      <p>No points</p>
      {/if}
      {import assoc/Footer as $footer}
    </body>
    </html>
    
Compile this to the target src-gen directory with the "template" top level namespace 
and this creates a class `template/index/MyTemplate.java` with the fully qualified name 
`template.index.MyTemplate`.

The generated code might look something like this:

    package template.template.index; 
    
    class MyTemplate {
        private String title = "";
        private String headline = "";
        private boolean showIntroduction;
        private String introduction = "";
        private List<Point> pointList = new ArrayList<>();
        private templates.assoc.Footer footer; 
        
        public MyTemplate() {}
        public MyTemplate(String title, ...) {this.title = title; ...}
        
        public MyTemplate setTitle(String title) {this.title = title;}
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
            sb.append(html(ellipsize80(this.headline)));
            sb.append("</title>\n    </head>");
            ...
            if (this.showIntroduction) {
                sb.append("<div>");
                sb.append(html(this.introduction));
                sb.append("</div>");
            }
            ...
            sb.toString();
        }
        
        public static class Point {
        
            private String target = "";
            private String text = "";
            
            public Point() {}
            
            public Point(String target, String text) { 
                this.target = target;
                this.text = text; 
            }
            
            public Point setTarget(String target) {...}
            ...
        }
    }

The methods toString or write are used to execute the template and get the result. So
in your code you could do:

    List<Points> points = Arrays.asList(
        new Point().setTarget("current news").setText("news"),
        new Point().setTarget("archives").setText("archives"));

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
 
* There are some pre-defined modifiers but additional ones can be added.

* No reflection at runtime.

## Benchmark

* It's fast. On my computer, the https://github.com/mbosecke/template-benchmark's 
  fastest engine is Rocker, with 49.271 ops/s. Temporize gets 66.275 ops/s.  
* Compilation may not as fast but that doesn't really matter because it happens at 
  project compile time instead of runtime
  
  
    Benchmark                     Mode  Cnt      Score     Error  Units
    Freemarker.benchmark         thrpt   50  20334,079 ± 173,635  ops/s
    Handlebars.benchmark         thrpt   50  24007,420 ± 138,765  ops/s
    Mustache.benchmark           thrpt   50  26920,209 ± 104,248  ops/s
    Pebble.benchmark             thrpt   50  43074,752 ± 316,106  ops/s
    Rocker.benchmark             thrpt   50  49271,911 ± 322,755  ops/s
    Temporize.benchmark          thrpt   50  66275,758 ± 458,256  ops/s
    Thymeleaf.benchmark          thrpt   50   1907,132 ±  24,483  ops/s
    Trimou.benchmark             thrpt   50  28742,674 ± 232,416  ops/s
    Velocity.benchmark           thrpt   50  26137,320 ± 141,420  ops/s

## How to use



### Call manually

Converts the raw templates into classes on execution. Per default only 
files with a `.temporize.` in the name are processed.  

1. parameter: input directory where the raw templates are stored
2. parameter: the output directory where to store the classes
3. parameter: the fully qualified name of the class containing static modifier methods.  
   The `Modifiers` doesn't reference a real file, only an `import static to add to the files.`    

E.g.

 `java -jar temporize.jar tpl/ src_gen/ package.name.of.Modifiers`

### gradle pre-build

    task temporize() << {
        javaexec {
            main = "-jar"
            args = [
                    "temporize.jar",
                    "tpl",
                    "src/main/tpl",
                    "package.name.of.Modifiers"
            ]
        }
    }
     
    build.dependsOn temporize

## TODOs

* add boolean parameters if they only appear in conditionals
* sanity checks to possible variable and method names defined in templates  

# Temporize

Temporize is a template engine for java that statically compiles templates into java source files, which can then be 
used from your code without the original template files.

Compared to other engines Temporize is not necessarily designed to separate programmers from page designers as the 
project needs to be recompiled after changes in the templates.  

## Why + advantages

* Speed - it's ~30%-50% faster than Rocker at the https://github.com/mbosecke/template-benchmark Stock benchmark
* No file IO during runtime (i.e. no blocking, IO is done only during the compilation step) (except for the JVMs 
  startup itself)
* IDE auto-completion support for all IDEs without any extra plug-ins
* Packaging and distributing the compiled templates is easy. There is no need to package the original template sources.
* Bytecode obfuscators work on the templates.

## Limitations

* After changing templates the project must be recompiled
* The markup language is not very sophisticated, e.g. 
  * conditionals can have a single condition that is true if "assigned and not empty" (or assignable booleans, if the
    conditionals aren't used as placeholders or blocks)
  * placeholder values are strings only
* Generated templates are NOT exact concerning whitespace. Currently, all line endings are converted to unix-style
  line endings (`\n`). Also, a trailing line ending is appended at the end of the file. 
 
## Supported markup
 
* `{$placeholder}`: creates a placeholder for a string value.
* `{$placeholder|modifier1|modifier2}`: setter that also applies the given modifiers to the value.
  Modifiers are static String->String functions defined in a special class.
* `{*$placeholder}`: creates a placeholder for a string value with a "def" Modifier added.
  This is usable for common escaping or encoding actions, e.g. html-entity encoding.
* `{for $block}...{/for}`: creates a subclass from the content that can be assigned 0-n times.
* `{include com.example.mytemplate as $mytemplate}`: imports a different template here.
  the import gets its own top level class and can be re-used in different templates.
* `{if $condition}...{/if}`: creates a conditional. currently, conditionals only check for 
  empty strings or lists, depending on the type of `$condition`, or they're booleans, if not used anywhere else. 
* `{if $condition}...{else}...{/if}`: creates a conditional with an alternative.
* `{skip}...{/skip}` to exclude whole regions from parsing. Everything inside `skip` sections is emitted as a literal.
  Note that `skip` sections can't be nested. 
* `{comment}...{/comment}` to exclude whole regions from processing - nothing is emitted. 
  Note that `comment` sections can't be nested. 

## Example

Template `tpl/index/MyTemplate.temporize.html`:

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
      {include inc.Footer as $footer}
    </body>
    </html>
    
Compile this to the target src-gen directory by invoking
 
    java -jar temporize.jar tpl/ src/main/tpl-gen/ my.Modifiers
 
This creates a class `src/main/tpl-gen/index/MyTemplate.java`.

The generated code might look something like this:

    package index; 
    
    class MyTemplate {
        private String title = "";
        private String headline = "";
        private boolean showIntroduction;
        private String introduction = "";
        private List<Point> pointList = new ArrayList<>();
        private inc.Footer footer; 
        
        public MyTemplate() {}
        public MyTemplate(String title, ...) {this.title = title; ...}
        
        public MyTemplate setTitle(String title) {this.title = title;}
        ...
        public MyTemplate setShowIntroduction(boolean showIntroduction) {...}
        public MyTemplate setPointList(List<Point> pointList) {...}
        public MyTemplate setFooter(inc.Footer footer) {...}
        ...
        public MyTemplate write(Writer writer) { 
            /* same as toString() below, just with a writer instead of a 
               StringBuilder. */
        }
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
        new Point().setTarget("/news").setText("News"),
        /* or alternatively */
        new Point("/arch", "Archives"));

    System.out.print(new MyTemplate()
        .setTitle("hello world")
        .setHeadline("welcome, stranger")
        .showIntroduction(true)
        .setIntroduction("stay a while and listen!")
        .setPointList(points)
        .setFooter(new Footer().setCopyright("2017"));

or, even faster: 

    Writer writer = new StringWriter();

    new MyTemplate()
        .setTitle("hello world")
        .setHeadline("welcome, stranger")
        .showIntroduction(true)
        .setIntroduction("stay a while and listen!")
        .setPointList(points)
        .setFooter(new Footer().setCopyright("2017"))
        .write(writer);
        
     System.out.print(writer.toString());

## Benchmark

* It's fast. On my computer, the https://github.com/mbosecke/template-benchmark's 
  fastest engine is Rocker, with 49.072 ops/s. Temporize gets 76.054 ops/s.  

  
    Benchmark                     Mode  Cnt      Score      Error  Units
    Freemarker.benchmark         thrpt   50  20428,242 ±  129,919  ops/s
    Handlebars.benchmark         thrpt   50  23626,078 ±  237,292  ops/s
    Mustache.benchmark           thrpt   50  26602,105 ±  221,318  ops/s
    Pebble.benchmark             thrpt   50  43328,502 ±  375,950  ops/s
    Rocker.benchmark             thrpt   50  49072,283 ±  499,747  ops/s
    TemporizeToString.benchmark  thrpt   50  53945,231 ±  309,228  ops/s
    TemporizeWriter.benchmark    thrpt   50  76054,989 ± 1351,153  ops/s
    Thymeleaf.benchmark          thrpt   50   1888,219 ±   28,010  ops/s
    Trimou.benchmark             thrpt   50  28877,322 ±  258,909  ops/s
    Velocity.benchmark           thrpt   50  25800,604 ±  230,428  ops/s


## How to use

### Converting manually

Convert the raw templates into classes on execution. Per default only 
files with a `.temporize.` in the name are processed.  

1. parameter: input directory where the raw templates are stored
2. parameter: the output directory where to store the classes
3. parameter: the fully qualified name of the class containing static modifier methods.  
   The `Modifiers` doesn't reference an existing file, it only adds an `import static package.name.of.Modifiers.*;` #
   import.

#### Example

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

Don't forget to substitute for your actual paths.

## Known bugs and TODOs

* Stricter line ending handling. Currently only linux `\n` are generated, `\r`s are lost. 
* sanity checks for invalid variable and method names defined in templates
* have a go at continuous template builds via gradle: https://docs.gradle.org/current/userguide/continuous_build.html
  or java: https://docs.oracle.com/javase/tutorial/essential/io/notification.html
* Implement multi line `skip` and `comment`.
* Add file existence check for `include`s. 

## License

This software is published under the [MIT License](LICENSE.txt)
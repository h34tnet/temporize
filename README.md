# Temporize

* [Changelog](CHANGELOG.md)

## What is Temporize

Temporize is a template engine for java that statically compiles templates into java source files, which then can then 
be used from your code without the original template files.

Compared to other engines Temporize is not necessarily designed to separate programmers from designers as the 
project needs to be recompiled after changes in the templates.

## Why + advantages

* Speed - it's one of the - if not the - fastest engine in the https://github.com/mbosecke/template-benchmark Stock 
  benchmark. It's fast because the templates itself are compiled by the JVM. 
* No file IO during runtime (i.e. no blocking, IO is done only during the compilation step - except for the JVMs 
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
* `{include com.example.MyTemplate as $mytemplate}`: imports a different template here.
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

```html
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
```
    
Compile this to the target src-gen directory by invoking
 
    java -jar temporize.jar tpl/ src/main/tpl-gen/ my.Modifiers
 
This creates a class `src/main/tpl-gen/index/MyTemplate.java`.

The generated code might look something like this:

```java
package index; 

import static my.Modifiers.*;

class MyTemplate {
    private String title = "";
    private String headline = "";
    private boolean showIntroduction;
    private String introduction = "";
    private List<Point> pointList = new ArrayList<>();
    private inc.Footer footer; 
    
    public MyTemplate() {}
    public MyTemplate(String title /*, ... */) {this.title = title; /* ... */}
    
    public MyTemplate setTitle(String title) {this.title = title;}
    // ...
    public MyTemplate setShowIntroduction(boolean showIntroduction) { 
        this.showIntroduction = showIntroduction; 
        return this; 
    }
    
    public MyTemplate setPointList(List<Point> pointList) { /* ... */ }
    public MyTemplate setFooter(inc.Footer footer) { /* ... */ }
    // ...
    public MyTemplate write(Writer writer) { 
        /* same as toString() below, just with a writer instead of a 
           StringBuilder. */
    }
    // ...
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n    <head>\n      <title>");
        sb.append(html(ellipsize80(this.headline)));
        sb.append("</title>\n    </head>");
        // ...
        if (this.showIntroduction) {
            sb.append("<div>");
            sb.append(html(this.introduction));
            sb.append("</div>");
        }
        // ...
        return sb.toString();
    }
    
    public static class Point {
    
        private String target = "";
        private String text = "";
        
        public Point() {}
        
        public Point(String target, String text) { 
            this.target = target;
            this.text = text; 
        }
        
        public Point setTarget(String target) { /* ... */ }
        // ...
    }
}
```

The methods toString or write are used to execute the template and get the result. So
in your code you could do:

```java
public class Test {
    public static void main(String... args) {
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
            .setFooter(new Footer().setCopyright(2017 ))
            .toString());
    }
}
```

or, for example



You still have to provide a `my.Modifiers` class with the used String -> String 
methods - in the example `html`, `stripnl` and `ellipsize80`.

e.g.

```java
package my;

public class Modifiers {

    // (...)

    public static String ellipsize80(String in) {
        return in.length() < 80 ? in : in.substring(0, 76) + " ...";
    }
}
```

Modifiers can't take arguments yet. 

## Benchmark

* It's fast. On my computer, the [github.com/mbosecke/template-benchmark)](https://github.com/mbosecke/template-benchmark)'s 
  fastest engine is Rocker, with 49.738 ops/s. Temporize gets 77.176 ops/s.  

```
# Run complete. Total time: 00:12:43

Benchmark                     Mode  Cnt      Score      Error  Units
Freemarker.benchmark         thrpt   50  20358,267 ±   97,149  ops/s
Handlebars.benchmark         thrpt   50  23952,848 ±  177,972  ops/s
Mustache.benchmark           thrpt   50  26739,677 ±  156,825  ops/s
Pebble.benchmark             thrpt   50  41518,287 ± 1602,898  ops/s
Rocker.benchmark             thrpt   50  49738,370 ±  410,952  ops/s
TemporizeToString.benchmark  thrpt   50  77176,539 ±  791,690  ops/s
TemporizeWriter.benchmark    thrpt   50  76905,692 ±  311,332  ops/s
Thymeleaf.benchmark          thrpt   50   1912,023 ±   24,712  ops/s
Trimou.benchmark             thrpt   50  28922,202 ±  185,427  ops/s
Velocity.benchmark           thrpt   50  25620,993 ±  457,921  ops/s
```
## How to use

### Build locally

Temporize is now a maven plugin. Check it out from github and built it by 
executing:

    mvn clean install 

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
 
 
#### jitpack

[![](https://jitpack.io/v/h34tnet/temporize.svg)](https://jitpack.io/#h34tnet/temporize)

### maven plugin

Add temporize as a plugin to your project; the `<execution>` adds it to your `generate-sources` target.

Note: The `org.codehaus.mojo.build-helper-maven-plugin` adds the `gen` directory to the sources. This is optional if you
don't use a `src/gen/java` directory and add it to the main sources instead. 

```xml
<project>
    <pluginRepositories>
        <pluginRepository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </pluginRepository>
    </pluginRepositories>
    <!-- (...) -->
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.h34tnet</groupId>
                <artifactId>temporize</artifactId>
                <version>release-1.1.1</version>
                <configuration>
                    <inputPath>${project.basedir}/tpl</inputPath>
                    <outputPath>${project.basedir}/src/gen/java</outputPath>
                    <modifier>my.project.foobar.Modifiers</modifier>
                </configuration>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>temporize</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <version>3.0.0</version>
                <executions>
                    <execution>
                        <id>add-source</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>add-source</goal>
                        </goals>
                        <configuration>
                            <sources>
                                <source>${basedir}/src/gen/java</source>
                            </sources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>        
        </plugins>
    </build>
</project>
```

Don't forget to substitute for your actual paths and packages.

## Known bugs and TODOs

* Stricter line ending handling. Currently only linux `\n` are generated, `\r`s are lost. 
* Sanity checks for invalid variable and method names defined in templates.
* Add file existence check for `include`s. 
* Add stricter handling of file encodings.
* Clean up leftover java files from removed/renamed templates.
* Add option for debug output (--verbose) to the mojo plugin.
* Modifier arguments?

* Publish on ~~maven central and/or~~ jitpack (done)
* Implement multi line `skip` and `comment` (done)

## License

This software is published under the [MIT License](LICENSE)
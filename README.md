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
    private final String headline = "";
    private boolean showIntroduction;
    private final String introduction = "";
    private final List<Point> pointList = new ArrayList<>();
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
  fastest engine is [Rocker](https://github.com/fizzed/rocker), with ~73.000 ops/s. Temporize gets ~82.000ops/s.  

```
# Run complete. Total time: 00:11:32

Benchmark              Mode  Cnt      Score     Error  Units
Freemarker.benchmark  thrpt   50  19362,583 ± 522,458  ops/s
Handlebars.benchmark  thrpt   50  21247,319 ± 586,688  ops/s
Mustache.benchmark    thrpt   50  23228,965 ± 279,113  ops/s
Pebble.benchmark      thrpt   50  34855,337 ± 304,129  ops/s
Rocker.benchmark      thrpt   50  73244,777 ± 366,655  ops/s
Temporize.benchmark   thrpt   50  82259,467 ± 310,212  ops/s
Thymeleaf.benchmark   thrpt   50   5932,226 ±  95,475  ops/s
Trimou.benchmark      thrpt   50  26623,900 ± 232,096  ops/s
Velocity.benchmark    thrpt   50  23145,856 ± 160,639  ops/s
```

> **Note:** Rocker uses the same principle (precompilation). I didn't know about it when I started
  this project. While Rocker was slower in the beginning it now is almost as fast as Temporize (a negligible 
  difference) but much more fully featured. 
> 
> If you're looking for production use, I'd recommend Rocker over Temporize.   
 
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
                <version>release-1.1.3</version>
                <configuration>
                    <inputPath>${project.basedir}/tpl</inputPath>
                    <!-- Optional, this is the default output directory -->
                    <outputPath>${project.build.directory}/generated-sources/temporize</outputPath>
                    <modifier>my.project.foobar.Modifiers</modifier>
                </configuration>
                <executions>
                    <execution>
                        <id>temporize</id>
                        <!-- Optional -->
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>generate-templates</goal>
                        </goals>
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
* Add file existence check for `include`s. 
* Add stricter handling of file encodings.
* Add option for debug output (--verbose) to the mojo plugin.
* Modifier arguments?

* Sanity checks for invalid variable and method names defined in templates. (mostly done)
* Clean up leftover java files from removed/renamed templates. (done)
* Publish on ~~maven central and/or~~ jitpack (done)
* Implement multi line `skip` and `comment` (done)

## License

This software is published under the [MIT License](LICENSE)
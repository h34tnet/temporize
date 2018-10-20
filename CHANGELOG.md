# Release change log

## Version 1.1.1

* Writes classes only after all templates compiled successfully.
* Adds checks to prevent the use of reserved words in package names.
* Changes almost all File Operations from `java.io` to `java.nio`.
* Moves static void main(...) to class CliRunner
* Removes old compiled templates before compiling anew. This gets rid of java files
  originating from moved or renamed templates. 

## release-1.1

The first _real_ release, as the short-lived 1.0 suffered from package name confusion: 

Jitpack.io requires a different group name than your library actually has, because it's used to refer to your
repository. This works well with libraries, but not with plugins as the real group id and the groupId given in the 
`<plugin>` section must match.  
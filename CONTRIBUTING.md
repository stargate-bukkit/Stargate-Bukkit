The following guidelines was mainly made to make the plugin more maintainable

#### Always use descriptive names
Comments are good, but most that are made can just be replaced with a descriptive variable/method/class name. A good approach is to use comments to describe why you are doing things, but let the code describe what you are doing.
Methodnames = verbs
classnames = nouns
booleann variables = a name that gives a y/n answer
#### A method should never be longer than 20 lines, neither have more than 4 indents
A good aproach is to strive for modularity, where every method has a clear function. This is mainly achieved by just checking the length of the module you're writing, and if it's getting to long to just split it into two different functions.
#### Avoid exess dataflow between classes
Restricting dataflow makes the code much more maintable; This is mainly achived by starting with a plan before you code, but also to sometimes just to consider if a method fits into specific class. Getters and setters are usally a sign that too much data is being transmited between classes.
#### If you think the code needs a refactor, it most probably does
Refactors just keeps the code cleaner, and is almost always good. 

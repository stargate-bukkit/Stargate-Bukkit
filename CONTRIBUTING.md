The following guidelines was mainly made to make the plugin more maintainable. These points are what I (Thorin) consider general good programming practise, and does of course not have to be the "truth"

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
Refactors just keeps the code cleaner, and is almost always good. They are often easier to make than you think
#### A note about sideeffects
When you modularise your methods it will be simpler to understand or debug the code if you devide it into methods that does calculations and returns a value and methods that only does sideeffects. I have also noticed that this often automatically makes it easier to not rewrite code.
#### Don't know what you're doing? Read the manual!
I don't think it's a good idea if you mash your head against a wall until you find a solution to your problem. You will get pissed, and it might even be so that you don't understand your solution

*Programer 1: How does this work?

*Programer 2: I don't know

Nobody knows, and thats of course not going to be clearly coded. It's always more efficient to just research about your problem if you have come to a stop or even do the research before you code.

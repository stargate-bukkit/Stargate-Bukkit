The following guidelines have been written in hopes of making the plugin more maintainable.<md>
They represent what we consider to be good programming practices, and are not necessarily definitive.<md>

## Submitting your code.

Please work in a fork of the repository!<br><br>
All changes should be made in a branch called `feature:#` or `fix:#` (with # being
the [issue](https://github.com/the-lockedcraft-legacy-organization/Stargate-Bukkit/issues) your changes are correlated
to).<br>
Once you have tested your change, please make a pull
request [here](https://github.com/the-lockedcraft-legacy-organization/Stargate-Bukkit/pulls).

## Code Style

#### Always use descriptive names.

> Comments are great, however, they can generally be replaced with descriptive variable/method/class naming.

Please use comments to describe why you are using something, but let the code describe what you are doing.<br>Method
names should be verbs, class names should be nouns, and Boolean variables should be yes/no statements '(i.e. isRed)'.

#### Keep your methods short.

> We strive for modularity, with every method correlated to a clear function.

Please try to keep your methods under 20 lines, with less than 4 indents.<br>Check the length of the module you are
writing; if it is getting too long, please split it into different functions.

#### Avoid excess dataflow between classes.

> Restricting dataflow makes the code much more maintainable.

Please plan before you code, and consider what methods fit into each class.<br>Getters and setters are usually a sign
that too much data is being transmitted between classes.

#### Don’t be afraid of refactors.

> Refactors lead to cleaner code, which makes maintenance easier for everyone involved.

They are often less scary than they seem and are almost always worthwhile.

#### On side effects.

> When methods are modularised, they become simpler to understand and debug.

Please divide your code into methods for calculations and methods for side effects.

#### If you are unsure about something, take the time to ask/read up on it.

> Mashing your head against a wall to find a solution generally results in poor implementations.

If you hit a block, please look around for an answer; it will help everyone in the long term.


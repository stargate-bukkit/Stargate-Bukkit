# The complete refactor of stargate

Ragnarok is calling.... Change is futile

## Why would stargate need a complete refactor?

Stargate is an old plugin and has therefore gone through many changes. Most methods has slowly shifted away from their innitial ment behavior, and would therefore now be seen as a "missplaced" method, this is covered upp with loads of set and get methods which makes the plugin harder to modify. Some methods have also grown to unhealthy sizes (portal.gate for example). Some classes are also uneccesary as there now alrady exist spigot/paper classes that do the same thing.

This makes the whole plugin a mess that is hard to understand; which makes adding new features a pain

## Approach to do this as efficiently as possible
- [X] Start from blank
- [ ] Note down everythin stargate does, create a new class structure according to this
- [ ] Look through olds methods and assign good enough methods to appropiate spots
- [ ] Write new code to fill the holes, whilst not being afraid of doing refactors
- [ ] Do debugging
- [ ] Laugh at old stargate

# Stargates behaviors

## Gates
- Chose gatevariant; [insert gatetypes here]
- Support all those gatevariants
- Chose network and gatename
- Permissions to create gatetype on specific network
- Load every gatetype from all the .gate files
- Everytime someone places a sign, check for gatecreation
- Look through available gatetypes and chose the right gate to create (pattern matcher)
- Be able to charge player for creating gates, this should depend on gatetype
- Enable gate if button is pushed (if not always on)
- Create basic gate.gate file and watergate.gate file if not exists
- Store location of all current gates in a file
## Teleportation
- Check if player has permissions to acces network
- Check if player is in a gate and teleport to gate destination
- [removed] teleport entities
## Language
- Write player messages according to language files
- Be able to have any languages 
- [New possible feature] Add induvidual languages, each player can chose the language they want 
- Write new lang file if not exist
- Check loaded file through errors and correct them (still unclear)
## Economy
- Support Vault
Theres probably more plugins that have interactions with stargate, and it would be nice if those could stay
## API
- Add an API to stargate

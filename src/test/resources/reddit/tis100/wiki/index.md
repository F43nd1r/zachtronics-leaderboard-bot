# TIS-100 leaderboard
This page displays the best known solutions to all official levels. It is maintained by a [bot](https://github.com/F43nd1r/zachtronics-leaderboard-bot), which collects submitted solutions into a [repository](https://github.com/12345ieee/tis100-leaderboard) and generates the page seen here.

# What is counted?
The repository will accept any solution that is "pareto optimal", meaning that no known solution beats or ties it on every metric simultaneously.  
However, the only solutions displayed on this page are those which hold one of the category records. These records are credited to the first person to submit a solution with that behavior.  
For some of the early puzzles, the record solution was apparent to enough people at once that it is credited to "Community".

# Columns
One column holds records for each primary metric (Cycles, Nodes, Instructions). These scores are measured in the same way as by the game itself.

Within each column, up to 3 solutions may be shown per puzzle according to 3 different tiebreakers.  
For example, in the nodes column, there are separate records for the minimum nodes solution with lowest cycles, the minimum nodes solution with lowest instructions, and the minimum nodes solution with the lowest value for cycles\*instructions.

# Submitting and accessing solutions
The 📄 icons are links to the solution files, which can be opened in-game.

To add your solution to the bot, join the discord server at https://discord.gg/98QNzdJ and invoke the Leaderboard bot via `/tis submit solution:<export link> puzzle:<puzzle name> author:<your name>`.  
Use `m1` for the solution link to refer to the preceding message if that's how you uploaded the export.  
You can use the optional `image` parameter to give a screenshot of your solution, showing the code.  
The solutions are evaluated by the [TIS simulator](https://github.com/killerbee13/TIS-100-CXX) and scored automatically.

If you want to import a previously stored solution into your own game, you can find it by following the relevant link in this table (if it is a record solution); or by going to the Discord Server and invoking the Leaderboard Bot via `/tis frontier puzzle:<puzzle name>`; or by going to the leaderboard git repository and navigating to the campaign, level and solution you want.  
The files can be copied directly into the game's save folder and opened in-game.

# Cheats
Cheats are defined as:

- Anything that will ever fail on a random test. Legit solutions must catch all reasonable inputs.
- Designed to behave differently on the fixed input test from the random tests, such as by outputting hardcoded values and then hoping for a lucky random test.

A cheated solution will still be uploaded to the repository, but with a `/c` flag to designate this behavior. They will never overwrite a legitimate solution to the same level. A separate table for cheated solutions which have been proven to pass one random test is included on this page.

Solutions with a pass rate below 5% are tagged `/h` for hardcoding, they too are in the cheating table.

# Achievements
Certain [achievements in game](https://steamcommunity.com/stats/370360/achievements) require creating suboptimal solutions, such as BUSY_LOOP. For these puzzles, an extra table on this page shows the most optimized solutions that also get this achievement.

# TIS-100 SEGMENT MAP

| Puzzle | Cycles | Nodes | Instructions
| --- | --- | --- | --- | ---
| [SELF-TEST DIAGNOSTIC](https://zlbb.faendir.com/tis/00150) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.83-8-8.txt) [**83**/8/8 Community](https://i.imgur.com/GORtdg6.png) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.83-8-8.txt) [83/**8**/8 Community](https://i.imgur.com/GORtdg6.png) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.83-8-8.txt) [83/8/**8** Community](https://i.imgur.com/GORtdg6.png)
|
| [SIGNAL AMPLIFIER](https://zlbb.faendir.com/tis/10981) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/10981/10981.84-5-9.txt) **84**/5/9 Community | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/10981/10981.102-4-24.txt) 102/**4**/24 GuiltyBystander | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/10981/10981.160-4-6.txt) 160/4/**6** Community
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/10981/10981.160-4-6.txt) 160/**4**/6 Community |
|
| [DIFFERENTIAL CONVERTER](https://zlbb.faendir.com/tis/20176) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.126-10-30.txt) **126**/10/30 Ling Ling | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.181-5-16.txt) 181/**5**/16 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.237-5-10.txt) 237/5/**10** Brian142857
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.237-5-10.txt) 237/**5**/10 Brian142857 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.200-5-11.txt) 200/**5**/11 Community |
|
| [SIGNAL COMPARATOR](https://zlbb.faendir.com/tis/21340) | **195**/6/71 jpgrossman | 195/**6**/71 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/21340/21340.296-6-15.txt) 296/6/**15** jpgrossman
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/21340/21340.296-6-15.txt) 296/**6**/15 jpgrossman |
|
| [SIGNAL MULTIPLEXER](https://zlbb.faendir.com/tis/22280) | **148**/9/54 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/22280/22280.236-5-19.txt) 236/**5**/19 ImprobableFish | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/22280/22280.239-7-14.txt) [239/7/**14** longingforrest/Hersmunch](https://imgur.com/a/PJAPWbT)
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/22280/22280.262-5-15.txt) 262/**5**/15 Snowball | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/22280/22280.287-6-14.txt) [287/6/**14** longingforrest](https://imgur.com/a/F0ksmKZ)
|
| [SEQUENCE GENERATOR](https://zlbb.faendir.com/tis/30647) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/30647/30647.86-8-36.txt) **86**/8/36 pusalieth | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/30647/30647.110-4-20.txt) 110/**4**/20 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/30647/30647.140-5-13.txt) 140/5/**13** jpgrossman
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/30647/30647.155-4-16.txt) 155/**4**/16 Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/30647/30647.119-4-18.txt) 119/**4**/18 dionadar |
|
| [SEQUENCE COUNTER](https://zlbb.faendir.com/tis/31904) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/31904/31904.112-8-41.txt) **112**/8/41 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/31904/31904.173-4-24.txt) 173/**4**/24 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/31904/31904.203-5-14.txt) [203/5/**14** Hersmunch](https://imgur.com/6Ad3rwf)
|  | **112**/9/32 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/31904/31904.222-4-14.txt) 222/**4**/14 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/31904/31904.222-4-14.txt) 222/4/**14** Brian142857
|
| [SIGNAL EDGE DETECTOR](https://zlbb.faendir.com/tis/32050) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.102-11-110.txt) **102**/11/110 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.175-4-53.txt) 175/**4**/53 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.313-4-14.txt) 313/4/**14** ImprobableFish
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.313-4-14.txt) 313/**4**/14 ImprobableFish |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.240-4-15.txt) 240/**4**/15 ImprobableFish |
|
| [INTERRUPT HANDLER](https://zlbb.faendir.com/tis/33762) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.122-11-34.txt) **122**/11/34 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.234-6-47.txt) 234/**6**/47 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.397-6-21.txt) 397/6/**21** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.397-6-21.txt) 397/**6**/21 Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.303-6-23.txt) 303/**6**/23 Hersmunch |
|
| [SIGNAL PATTERN DETECTOR](https://zlbb.faendir.com/tis/40196) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/40196/40196.82-8-16.txt) **82**/8/16 jpgrossman | 119/**4**/57 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/40196/40196.173-4-13.txt) 173/4/**13** jpgrossman
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/40196/40196.173-4-13.txt) 173/**4**/13 jpgrossman |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/40196/40196.155-4-14.txt) 155/**4**/14 Brian142857 |
|
| [SEQUENCE PEAK DETECTOR](https://zlbb.faendir.com/tis/41427) | **195**/9/49 LowLevel\- | 269/**4**/35 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/41427/41427.648-4-21.txt) [648/4/**21** Hersmunch/Brian142857](https://imgur.com/FQiUu8t)
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/41427/41427.648-4-21.txt) [648/**4**/21 Hersmunch/Brian142857](https://imgur.com/FQiUu8t) |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/41427/41427.334-4-26.txt) 334/**4**/26 Hersmunch |
|
| [SEQUENCE REVERSER](https://zlbb.faendir.com/tis/42656) | **190**/7/49 jpgrossman | 252/**3**/26 LowLevel\- | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/42656/42656.314-3-8.txt) 314/3/**8** dionadar
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/42656/42656.314-3-8.txt) 314/**3**/8 dionadar |
|
| [SIGNAL MULTIPLIER](https://zlbb.faendir.com/tis/43786) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/43786/43786.228-9-75.txt) **228**/9/75 Hersmunch/Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/43786/43786.386-4-33.txt) 386/**4**/33 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/43786/43786.1510-4-14.txt) 1510/4/**14** Brian142857/Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/43786/43786.1510-4-14.txt) 1510/**4**/14 Brian142857/Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/43786/43786.434-4-23.txt) 434/**4**/23 ImprobableFish |
|
| [IMAGE TEST PATTERN 1](https://zlbb.faendir.com/tis/50370) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.1186-2-20.txt) **1186**/2/20 GuiltyBystander | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.1346-1-15.txt) 1346/**1**/15 GuiltyBystander | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.2282-1-7.txt) 2282/1/**7** GuiltyBystander
|  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.1186-3-10.txt) **1186**/3/10 ImprobableFish | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.2282-1-7.txt) 2282/**1**/7 GuiltyBystander |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.1742-1-8.txt) 1742/**1**/8 GuiltyBystander |
|
| [IMAGE TEST PATTERN 2](https://zlbb.faendir.com/tis/51781) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/51781/51781.1150-3-15.txt) **1150**/3/15 Untellect | 1418/**1**/15 MAKESPEARE | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/51781/51781.1221-3-9.txt) 1221/3/**9** rhinospray
|  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/51781/51781.1150-4-13.txt) **1150**/4/13 biggiemac42 | 3596/**1**/9 MAKESPEARE | 3596/1/**9** MAKESPEARE
|  |  | 1778/**1**/11 MAKESPEARE |
|
| [EXPOSURE MASK VIEWER](https://zlbb.faendir.com/tis/52544) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/52544/52544.515-5-36.txt) **515**/5/36 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/52544/52544.549-4-58.txt) 549/**4**/58 Hersmunch | 1037/5/**25** jpgrossman
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/52544/52544.1087-4-26.txt) 1087/**4**/26 Hersmunch |
|  |  | 568/**4**/35 PocketLint2012 |
|
| [HISTOGRAM VIEWER](https://zlbb.faendir.com/tis/53897) | **1215**/11/142 PocketLint2012 | 1801/**4**/56 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/53897/53897.2773-5-14.txt) [2773/5/**14** Hersmunch](https://imgur.com/wPgPNAx)
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/53897/53897.2773-4-15.txt) 2773/**4**/15 Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/53897/53897.2466-4-16.txt) 2466/**4**/16 Grimmy |
|
| [SIGNAL WINDOW FILTER](https://zlbb.faendir.com/tis/60099) | **200**/7/22 \_Fluff\_ | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/60099/60099.313-4-19.txt) 313/**4**/19 jpgrossman | 392/4/**16** jpgrossman
|  |  | 392/**4**/16 jpgrossman |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/60099/60099.314-4-18.txt) 314/**4**/18 dionadar |
|
| [SIGNAL DIVIDER](https://zlbb.faendir.com/tis/61212) | **880**/9/112 jpgrossman | 1492/**5**/44 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/61212/61212.4488-6-19.txt) 4488/6/**19** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/61212/61212.6155-5-19.txt) 6155/**5**/19 \_Fluff\_ | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/61212/61212.6155-5-19.txt) 6155/5/**19** \_Fluff\_
|  |  | 1824/**5**/33 jpgrossman |
|
| [SEQUENCE INDEXER](https://zlbb.faendir.com/tis/62711) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.303-9-97.txt) **303**/9/97 trevdak2 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.926-4-50.txt) 926/**4**/50 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.2158-5-17.txt) 2158/5/**17** ImprobableFish
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.3173-4-21.txt) 3173/**4**/21 Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.932-4-39.txt) 932/**4**/39 Hersmunch |
|
| [SEQUENCE SORTER](https://zlbb.faendir.com/tis/63534) | **468**/7/97 jpgrossman | 1398/**3**/44 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/63534/63534.1503-4-26.txt) 1503/4/**26** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/63534/63534.2150-3-31.txt) [2150/**3**/31 Hersmunch](https://imgur.com/aAj8Ec2) |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/63534/63534.1541-3-35.txt) 1541/**3**/35 Hersmunch |
|
| [STORED IMAGE DECODER](https://zlbb.faendir.com/tis/70601) | **1190**/7/51 jpgrossman | 1371/**4**/41 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/70601/70601.2344-4-17.txt) 2344/4/**17** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/70601/70601.2344-4-17.txt) 2344/**4**/17 Hersmunch |
|  |  | 1635/**4**/21 jpgrossman |
|
| [UNKNOWN](https://zlbb.faendir.com/tis/UNKNOWN) | **247**/8/64 jpgrossman | 317/**4**/54 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/UNKNOWN/UNKNOWN.649-4-25.txt) 649/4/**25** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/UNKNOWN/UNKNOWN.649-4-25.txt) 649/**4**/25 Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/UNKNOWN/UNKNOWN.392-4-32.txt) 392/**4**/32 Hersmunch |
|
| **Totals** | **9107** | **95** | **346**

# TIS-NET DIRECTORY

| Puzzle | Cycles | Nodes | Instructions
| --- | --- | --- | --- | ---
| [SEQUENCE MERGER](https://zlbb.faendir.com/tis/NEXUS.00.526.6) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.283-6-33.txt) [**283**/6/33 Hersmunch](https://imgur.com/djxYANr) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.283-6-33.txt) [283/**6**/33 Hersmunch](https://imgur.com/djxYANr) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.370-6-22.txt) [370/6/**22** Hersmunch](https://imgur.com/RoQOIl9)
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.370-6-22.txt) [370/**6**/22 Hersmunch](https://imgur.com/RoQOIl9) |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.342-6-23.txt) [342/**6**/23 Hersmunch](https://imgur.com/qewpt4G) |
|
| [INTEGER SERIES CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.01.874.8) | **268**/9/135 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.01.874.8/NEXUS.01.874.8.855-3-32.txt) 855/**3**/32 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.01.874.8/NEXUS.01.874.8.4014-3-10.txt) 4014/3/**10** \_Fluff\_
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.01.874.8/NEXUS.01.874.8.4014-3-10.txt) 4014/**3**/10 \_Fluff\_ |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.01.874.8/NEXUS.01.874.8.1389-3-18.txt) 1389/**3**/18 Brian142857 |
|
| [SEQUENCE RANGE LIMITER](https://zlbb.faendir.com/tis/NEXUS.02.981.2) | **154**/11/99 jpgrossman | 259/**5**/44 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.02.981.2/NEXUS.02.981.2.654-5-24.txt) 654/5/**24** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.02.981.2/NEXUS.02.981.2.654-5-24.txt) 654/**5**/24 Hersmunch |
|  |  | 310/**5**/30 jpgrossman |
|
| [SIGNAL ERROR CORRECTOR](https://zlbb.faendir.com/tis/NEXUS.03.176.9) | **140**/10/54 jpgrossman | 256/**5**/21 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.03.176.9/NEXUS.03.176.9.437-5-14.txt) 437/5/**14** ImprobableFish
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.03.176.9/NEXUS.03.176.9.437-5-14.txt) 437/**5**/14 ImprobableFish |
|  |  | 290/**5**/16 jpgrossman |
|
| [SUBSEQUENCE EXTRACTOR](https://zlbb.faendir.com/tis/NEXUS.04.340.5) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.94-6-29.txt) **94**/6/29 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.112-4-32.txt) 112/**4**/32 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.160-4-18.txt) 160/4/**18** \_Fluff\_
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.160-4-18.txt) 160/**4**/18 \_Fluff\_ |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.113-4-24.txt) 113/**4**/24 jpgrossman |
|
| [SIGNAL PRESCALER](https://zlbb.faendir.com/tis/NEXUS.05.647.1) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.05.647.1/NEXUS.05.647.1.304-8-66.txt) **304**/8/66 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.05.647.1/NEXUS.05.647.1.398-6-50.txt) 398/**6**/50 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.05.647.1/NEXUS.05.647.1.10614-6-17.txt) 10614/6/**17** jpgrossman
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.05.647.1/NEXUS.05.647.1.10614-6-17.txt) 10614/**6**/17 jpgrossman |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.05.647.1/NEXUS.05.647.1.423-6-41.txt) 423/**6**/41 Brian142857 |
|
| [SIGNAL AVERAGER](https://zlbb.faendir.com/tis/NEXUS.06.786.0) | **538**/10/89 jpgrossman | 1164/**4**/56 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.06.786.0/NEXUS.06.786.0.88682-4-10.txt) 88682/4/**10** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.06.786.0/NEXUS.06.786.0.88682-4-10.txt) 88682/**4**/10 Hersmunch |
|  |  | 1201/**4**/49 jpgrossman |
|
| [SUBMAXIMUM SELECTOR](https://zlbb.faendir.com/tis/NEXUS.07.050.0) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.07.050.0/NEXUS.07.050.0.336-11-69.txt) [**336**/11/69 Hersmunch](https://imgur.com/a/3L30xv6) | 615/**6**/54 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.07.050.0/NEXUS.07.050.0.1729-10-33.txt) 1729/10/**33** Hersmunch
|  |  | 631/**6**/49 jpgrossman | 1755/7/**33** jpgrossman
|
| [DECIMAL DECOMPOSER](https://zlbb.faendir.com/tis/NEXUS.08.633.9) | **400**/10/104 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.08.633.9/NEXUS.08.633.9.701-5-58.txt) 701/**5**/58 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.08.633.9/NEXUS.08.633.9.7706-5-18.txt) 7706/5/**18** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.08.633.9/NEXUS.08.633.9.7706-5-18.txt) 7706/**5**/18 Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.08.633.9/NEXUS.08.633.9.745-5-40.txt) 745/**5**/40 Hersmunch |
|
| [SEQUENCE MODE CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.09.904.9) | **289**/7/102 jpgrossman | 1749/**3**/44 jpgrossman | 1801/3/**42** jpgrossman
|  |  | 1801/**3**/42 jpgrossman |
|  |  | 1752/**3**/43 jpgrossman |
|
| [SEQUENCE NORMALIZER](https://zlbb.faendir.com/tis/NEXUS.10.656.5) | **399**/8/86 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.10.656.5/NEXUS.10.656.5.665-3-40.txt) 665/**3**/40 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.10.656.5/NEXUS.10.656.5.725-4-27.txt) 725/4/**27** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.10.656.5/NEXUS.10.656.5.3548-3-29.txt) 3548/**3**/29 Hersmunch |
|  |  | 674/**3**/38 jpgrossman |
|
| [IMAGE TEST PATTERN 3](https://zlbb.faendir.com/tis/NEXUS.11.711.2) | **882**/10/80 PocketLint2012 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.11.711.2/NEXUS.11.711.2.2574-3-40.txt) 2574/**3**/40 biggiemac42 | 2746/5/**27** jpgrossman
|  |  | 2764/**3**/32 jpgrossman |
|
| [IMAGE TEST PATTERN 4](https://zlbb.faendir.com/tis/NEXUS.12.534.4) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.12.534.4/NEXUS.12.534.4.1166-6-34.txt) **1166**/6/34 Hersmunch | 1474/**2**/24 MAKESPEARE | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.12.534.4/NEXUS.12.534.4.1253-4-14.txt) 1253/4/**14** Hersmunch
|  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.12.534.4/NEXUS.12.534.4.1166-7-30.txt) **1166**/7/30 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.12.534.4/NEXUS.12.534.4.2289-2-17.txt) [2289/**2**/17 Brian142857/longingforrest](https://imgur.com/a/2CprZAo) |
|  |  | 1645/**2**/21 MAKESPEARE |
|
| [SPATIAL PATH VIEWER](https://zlbb.faendir.com/tis/NEXUS.13.370.9) | **745**/9/62 jpgrossman | 1050/**4**/55 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.13.370.9/NEXUS.13.370.9.1507-4-22.txt) 1507/4/**22** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.13.370.9/NEXUS.13.370.9.1507-4-22.txt) 1507/**4**/22 Hersmunch |
|
| [CHARACTER TERMINAL](https://zlbb.faendir.com/tis/NEXUS.14.781.3) | **590**/9/105 jpgrossman | 830/**4**/49 jpgrossman | 1007/6/**34** jpgrossman
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.14.781.3/NEXUS.14.781.3.1276-4-39.txt) 1276/**4**/39 Hersmunch |
|
| [BACK-REFERENCE REIFIER](https://zlbb.faendir.com/tis/NEXUS.15.897.9) | **227**/9/57 jpgrossman | 369/**4**/53 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.15.897.9/NEXUS.15.897.9.849-4-21.txt) 849/4/**21** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.15.897.9/NEXUS.15.897.9.849-4-21.txt) 849/**4**/21 Hersmunch |
|  |  | 736/**4**/22 jpgrossman |
|
| [DYNAMIC PATTERN DETECTOR](https://zlbb.faendir.com/tis/NEXUS.16.212.8) | **166**/11/56 jpgrossman | 250/**5**/56 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.16.212.8/NEXUS.16.212.8.647-8-23.txt) 647/8/**23** Hersmunch
|
| [SEQUENCE GAP INTERPOLATOR](https://zlbb.faendir.com/tis/NEXUS.17.135.0) | **106**/7/81 jpgrossman | 351/**3**/29 jpgrossman | 498/3/**21** jpgrossman
|  |  | 498/**3**/21 jpgrossman |
|
| [DECIMAL TO OCTAL CONVERTER](https://zlbb.faendir.com/tis/NEXUS.18.427.7) | **138**/11/105 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.18.427.7/NEXUS.18.427.7.277-4-50.txt) 277/**4**/50 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.18.427.7/NEXUS.18.427.7.955-4-12.txt) 955/4/**12** Brian142857
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.18.427.7/NEXUS.18.427.7.955-4-12.txt) 955/**4**/12 Brian142857 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.18.427.7/NEXUS.18.427.7.287-4-33.txt) 287/**4**/33 Brian142857 |
|
| [PROLONGED SEQUENCE SORTER](https://zlbb.faendir.com/tis/NEXUS.19.762.9) | **362**/8/90 PocketLint2012 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.19.762.9/NEXUS.19.762.9.2177-3-45.txt) 2177/**3**/45 Hersmunch | 2576/4/**28** jpgrossman
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.19.762.9/NEXUS.19.762.9.2581-3-37.txt) 2581/**3**/37 Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.19.762.9/NEXUS.19.762.9.2214-3-38.txt) 2214/**3**/38 Hersmunch |
|
| [PRIME FACTOR CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.20.433.1) | **409**/11/147 jpgrossman | 5924/**3**/38 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.20.433.1/NEXUS.20.433.1.17826-6-26.txt) [17826/6/**26** jpgrossman/Hersmunch](https://imgur.com/mh7BUs3)
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.20.433.1/NEXUS.20.433.1.10902-3-27.txt) 10902/**3**/27 Hersmunch |
|  |  | 6234/**3**/34 ImprobableFish |
|
| [SIGNAL EXPONENTIATOR](https://zlbb.faendir.com/tis/NEXUS.21.601.6) | **290**/8/119 jpgrossman | 1444/**4**/47 jpgrossman | 4256/4/**23** jpgrossman
|  |  | 4256/**4**/23 jpgrossman |
|
| [T20 NODE EMULATOR](https://zlbb.faendir.com/tis/NEXUS.22.280.8) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.168-9-49.txt) **168**/9/49 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.293-4-34.txt) 293/**4**/34 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.212-5-22.txt) 212/5/**22** ImprobableFish
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.318-4-23.txt) 318/**4**/23 Hersmunch |
|
| [T31 NODE EMULATOR](https://zlbb.faendir.com/tis/NEXUS.23.727.9) | **166**/9/108 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.738-3-36.txt) 738/**3**/36 Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.800-4-23.txt) 800/4/**23** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.874-3-31.txt) 874/**3**/31 Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.741-3-34.txt) 741/**3**/34 Hersmunch |
|
| [WAVE COLLAPSE SUPERVISOR](https://zlbb.faendir.com/tis/NEXUS.24.511.7) | **245**/12/57 jpgrossman | 428/**6**/53 jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.24.511.7/NEXUS.24.511.7.957-7-30.txt) 957/7/**30** Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.24.511.7/NEXUS.24.511.7.957-6-31.txt) 957/**6**/31 Hersmunch |
|  |  | 470/**6**/36 jpgrossman |
|
| **Totals** | **8865** | **102** | **561**

# Achievement Solutions

| Puzzle | Cycles | Nodes | Instructions
| --- | --- | --- | --- | ---
| [SELF-TEST DIAGNOSTIC](https://zlbb.faendir.com/tis/00150?visualizerFilterTIS-00150.modifiers.achievement=true) (BUSY_LOOP) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.100001-8-12-a.txt) **100001**/8/12/a Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.100001-8-12-a.txt) 100001/**8**/12/a Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.100001-8-12-a.txt) 100001/8/**12**/a Brian142857
|
| [SIGNAL COMPARATOR](https://zlbb.faendir.com/tis/21340?visualizerFilterTIS-21340.modifiers.achievement=true) (UNCONDITIONAL) | **210**/6/66/a jpgrossman | 210/**6**/66/a jpgrossman | 494/6/**16**/a jpgrossman
|  |  | 494/**6**/16/a jpgrossman |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/21340/21340.282-6-19-a.txt) 282/**6**/19/a Hersmunch |
|
| [SEQUENCE REVERSER](https://zlbb.faendir.com/tis/42656?visualizerFilterTIS-42656.modifiers.achievement=true) (NO_MEMORY) | **191**/7/50/a jpgrossman | 228/**4**/51/a jpgrossman | 685/5/**26**/a jpgrossman
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/42656/42656.478-4-35-a.txt) [478/**4**/35/a la3225/longingforrest](https://imgur.com/a/s8PrfRA) |
|  |  | 233/**4**/47/a jpgrossman |
|
| **Totals** | **100402** | **18** | **54**

# Cheating Solutions

| Puzzle | Cycles | Nodes | Instructions
| --- | --- | --- | --- | ---
| [SEQUENCE COUNTER](https://zlbb.faendir.com/tis/31904?visualizerFilterTIS-31904.modifiers.cheating=true) | **55**/11/147/c F1000003 |  |
|
| [SIGNAL EDGE DETECTOR](https://zlbb.faendir.com/tis/32050?visualizerFilterTIS-32050.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.80-9-98-h.txt) **80**/9/98/h biggiemac42 |  |
|
| [SIGNAL PATTERN DETECTOR](https://zlbb.faendir.com/tis/40196?visualizerFilterTIS-40196.modifiers.cheating=true) | **81**/9/56/c AmitaiG |  |
|
| [SEQUENCE PEAK DETECTOR](https://zlbb.faendir.com/tis/41427?visualizerFilterTIS-41427.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/41427/41427.30-11-150-h.txt) **30**/11/150/h biggiemac42 |  |
|
| [SEQUENCE REVERSER](https://zlbb.faendir.com/tis/42656?visualizerFilterTIS-42656.modifiers.cheating=true) | **185**/9/87/c Amitai |  |
|
| [SEQUENCE INDEXER](https://zlbb.faendir.com/tis/62711?visualizerFilterTIS-62711.modifiers.cheating=true) | **301**/9/102/c trevdak2 |  |
|
| [SEQUENCE SORTER](https://zlbb.faendir.com/tis/63534?visualizerFilterTIS-63534.modifiers.cheating=true) |  | 1361/**3**/44/c jpgrossman |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/63534/63534.2078-3-31-c.txt) 2078/**3**/31/c Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/63534/63534.1481-3-35-c.txt) 1481/**3**/35/c Hersmunch |
|
| [STORED IMAGE DECODER](https://zlbb.faendir.com/tis/70601?visualizerFilterTIS-70601.modifiers.cheating=true) |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/70601/70601.2707-4-16-c.txt) 2707/**4**/16/c Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/70601/70601.2707-4-16-c.txt) 2707/4/**16**/c Hersmunch
|
| [UNKNOWN](https://zlbb.faendir.com/tis/UNKNOWN?visualizerFilterTIS-UNKNOWN.modifiers.cheating=true) |  | 314/**4**/51/c jpgrossman | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/UNKNOWN/UNKNOWN.539-5-24-c.txt) 539/5/**24**/c longingforrest
|
| [SEQUENCE MERGER](https://zlbb.faendir.com/tis/NEXUS.00.526.6?visualizerFilterTIS-NEXUS-00-526-6.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.281-6-40-c.txt) **281**/6/40/c Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.281-6-40-c.txt) 281/**6**/40/c Hersmunch |
|
| [SUBSEQUENCE EXTRACTOR](https://zlbb.faendir.com/tis/NEXUS.04.340.5?visualizerFilterTIS-NEXUS-04-340-5.modifiers.cheating=true) |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.113-4-23-c.txt) 113/**4**/23/c longingforrest |
|
| [SIGNAL AVERAGER](https://zlbb.faendir.com/tis/NEXUS.06.786.0?visualizerFilterTIS-NEXUS-06-786-0.modifiers.cheating=true) |  | 1124/**4**/48/c jpgrossman |
|
| [SEQUENCE MODE CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.09.904.9?visualizerFilterTIS-NEXUS-09-904-9.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.09.904.9/NEXUS.09.904.9.23-3-33-h.txt) **23**/3/33/h negative\_seven | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.09.904.9/NEXUS.09.904.9.23-3-33-h.txt) 23/**3**/33/h negative\_seven | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.09.904.9/NEXUS.09.904.9.24-3-27-h.txt) 24/3/**27**/h negative\_seven
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.09.904.9/NEXUS.09.904.9.24-3-27-h.txt) 24/**3**/27/h negative\_seven |
|
| [SEQUENCE NORMALIZER](https://zlbb.faendir.com/tis/NEXUS.10.656.5?visualizerFilterTIS-NEXUS-10-656-5.modifiers.cheating=true) |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.10.656.5/NEXUS.10.656.5.661-3-41-c.txt) 661/**3**/41/c Hersmunch |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.10.656.5/NEXUS.10.656.5.3534-3-29-c.txt) 3534/**3**/29/c Hersmunch |
|
| [DYNAMIC PATTERN DETECTOR](https://zlbb.faendir.com/tis/NEXUS.16.212.8?visualizerFilterTIS-NEXUS-16-212-8.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.16.212.8/NEXUS.16.212.8.78-6-53-h.txt) **78**/6/53/h negative\_seven | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.16.212.8/NEXUS.16.212.8.149-3-23-h.txt) 149/**3**/23/h biggiemac42 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.16.212.8/NEXUS.16.212.8.355-5-20-c.txt) 355/5/**20**/c Hersmunch
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.16.212.8/NEXUS.16.212.8.355-5-20-c.txt) 355/**5**/20/c Hersmunch |
|
| [SEQUENCE GAP INTERPOLATOR](https://zlbb.faendir.com/tis/NEXUS.17.135.0?visualizerFilterTIS-NEXUS-17-135-0.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.17.135.0/NEXUS.17.135.0.29-6-70-h.txt) **29**/6/70/h negative\_seven |  |
|
| [PROLONGED SEQUENCE SORTER](https://zlbb.faendir.com/tis/NEXUS.19.762.9?visualizerFilterTIS-NEXUS-19-762-9.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.19.762.9/NEXUS.19.762.9.210-8-99-h.txt) [**210**/8/99/h biggiemac42](https://youtu.be/oW1zItkQ2us) |  |
|
| [PRIME FACTOR CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.20.433.1?visualizerFilterTIS-NEXUS-20-433-1.modifiers.cheating=true) |  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.20.433.1/NEXUS.20.433.1.15666-6-26-c.txt) 15666/6/**26**/c Hersmunch
|
| [SIGNAL EXPONENTIATOR](https://zlbb.faendir.com/tis/NEXUS.21.601.6?visualizerFilterTIS-NEXUS-21-601-6.modifiers.cheating=true) | **278**/8/116/c jpgrossman |  |
|
| [T20 NODE EMULATOR](https://zlbb.faendir.com/tis/NEXUS.22.280.8?visualizerFilterTIS-NEXUS-22-280-8.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.29-4-36-h.txt) **29**/4/36/h biggiemac42 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.40-3-34-h.txt) 40/**3**/34/h biggiemac42 |
|
| [T31 NODE EMULATOR](https://zlbb.faendir.com/tis/NEXUS.23.727.9?visualizerFilterTIS-NEXUS-23-727-9.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.21-3-26-h.txt) **21**/3/26/h Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.21-3-26-h.txt) 21/**3**/26/h Hersmunch | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.51-4-22-h.txt) 51/4/**22**/h cf
|
| **Totals** | **1681** | **32** | **135**

# Most record solutions

| Solutions | Name(s)
| --- | --- 
| 83.5 | jpgrossman
| 68.5 | Hersmunch
| 21 | Brian142857
| 9 | ImprobableFish
| 8 | biggiemac42
| 5 | GuiltyBystander, MAKESPEARE
| 4.5 | longingforrest
| 4 | \_Fluff\_, Community, negative\_seven, PocketLint2012
| 3 | dionadar
| 2 | LowLevel\-, trevdak2
| 1 | Amitai, AmitaiG, cf, F1000003, Grimmy, Ling Ling, pusalieth, rhinospray, Snowball, Untellect
| 0.5 | la3225

# Most frontier solutions

| Solutions | Name(s)
| --- | --- 
| 245.5 | Hersmunch
| 175 | Brian142857
| 115.5 | jpgrossman
| 54 | longingforrest
| 36 | Csaboka
| 26 | GuiltyBystander
| 22 | ImprobableFish
| 13 | biggiemac42
| 12 | Andrash, ShadowCluster
| 11 | Grimmy
| 6 | MAKESPEARE, negative\_seven
| 5 | \_Fluff\_, Community
| 4 | cf, PocketLint2012, rolamni, Snowball
| 3 | dionadar, rhinospray, starfish
| 2 | LowLevel\-, trevdak2, vcque
| 1 | Akari\_Takai, Amitai, AmitaiG, andy75381, bdekeijzer, Daniel\_Nson, davidtriphon, Entity\_, F1000003, Forgetful, foxyseta, gravypod, i\_dont\_want\_karma, ilyakharlamov, isaac\.wass, killerbee13, l10veu, la3225, Ling Ling, mlehmk, mr\_screg, notgreat, nsmaciej, proudHaskeller, pusalieth, shufb\_demon, Untellect

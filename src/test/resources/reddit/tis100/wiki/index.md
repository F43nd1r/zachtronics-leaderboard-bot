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

To add your solution to the bot, join the discord server at https://discord.gg/98QNzdJ and invoke the Leaderboard bot via `/tis submit solution:<export link> author:<your name> score:<ccc/nn/ii[/ac]>`.  
Use `m1` for the solution link to refer to the preceding message if that's how you uploaded the export.  
You can use the optional image parameter to give a screenshot of your solution, showing the code.

If you want to import a previously stored solution into your own game, you can find it by following the relevant link in this table (if it is a record solution); or by going to the Discord Server and invoking the Leaderboard Bot via `/tis frontier puzzle:<puzzle name>`; or by going to the leaderboard git repository and navigating to the campaign, level and solution you want.  
The files can be copied directly into the game's save folder and opened in-game.

# Cheats
Cheats are defined as:

- Anything that will ever fail on a random test. Legit solutions must catch all reasonable inputs.
- Designed to behave differently on the fixed input test from the random tests, such as by outputting hardcoded values and then hoping for a lucky random test.

A cheated solution will still be uploaded to the repository, but with a `/c` flag to designate this behavior. They will never overwrite a legitimate solution to the same level. A separate table for cheated solutions which have been proven to pass one random test is included on this page.

# Achievements
Certain achievements in game require creating suboptimal solutions, such as BUSY_LOOP. For these puzzles, an extra table on this page shows the most optimized solutions that also get this achievement.

# TIS-100 SEGMENT MAP

| Puzzle | Cycles | Nodes | Instructions
| --- | --- | --- | --- | ---
| [SELF-TEST DIAGNOSTIC](https://zlbb.faendir.com/tis/00150) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.83-8-8.txt) Community **83**/8/8 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.83-8-8.txt) Community 83/**8**/8 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/00150/00150.83-8-8.txt) Community 83/8/**8**
|
| [SIGNAL AMPLIFIER](https://zlbb.faendir.com/tis/10981) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/10981/10981.84-5-9.txt) Community **84**/5/9 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/10981/10981.102-4-24.txt) GuiltyBystander 102/**4**/24 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/10981/10981.160-4-6.txt) Community 160/4/**6**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/10981/10981.160-4-6.txt) Community 160/**4**/6 |
|
| [DIFFERENTIAL CONVERTER](https://zlbb.faendir.com/tis/20176) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.126-10-30.txt) Ling Ling **126**/10/30 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.181-5-16.txt) Brian142857 181/**5**/16 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.237-5-10.txt) Brian142857 237/5/**10**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.237-5-10.txt) Brian142857 237/**5**/10 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/20176/20176.200-5-11.txt) Community 200/**5**/11 |
|
| [SIGNAL COMPARATOR](https://zlbb.faendir.com/tis/21340) | jpgrossman **195**/6/71 | jpgrossman 195/**6**/71 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/21340/21340.296-6-15.txt) jpgrossman 296/6/**15**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/21340/21340.296-6-15.txt) jpgrossman 296/**6**/15 |
|
| [SIGNAL MULTIPLEXER](https://zlbb.faendir.com/tis/22280) | jpgrossman **148**/9/54 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/22280/22280.236-5-19.txt) ImprobableFish 236/**5**/19 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/22280/22280.298-6-14.txt) jpgrossman 298/6/**14**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/22280/22280.262-5-15.txt) Snowball 262/**5**/15 |
|
| [SEQUENCE GENERATOR](https://zlbb.faendir.com/tis/30647) | pusalieth **86**/8/36 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/30647/30647.110-4-20.txt) Brian142857 110/**4**/20 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/30647/30647.140-5-13.txt) jpgrossman 140/5/**13**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/30647/30647.155-4-16.txt) Hersmunch 155/**4**/16 |
|  |  | dionadar 119/**4**/18 |
|
| [SEQUENCE COUNTER](https://zlbb.faendir.com/tis/31904) | jpgrossman **112**/9/32 | jpgrossman 173/**4**/24 | Brian142857 222/4/**14**
|  |  | Brian142857 222/**4**/14 |
|
| [SIGNAL EDGE DETECTOR](https://zlbb.faendir.com/tis/32050) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.102-11-110.txt) Hersmunch **102**/11/110 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.175-4-53.txt) Hersmunch 175/**4**/53 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.313-4-14.txt) ImprobableFish 313/4/**14**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.313-4-14.txt) ImprobableFish 313/**4**/14 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.240-4-15.txt) ImprobableFish 240/**4**/15 |
|
| [INTERRUPT HANDLER](https://zlbb.faendir.com/tis/33762) | Brian142857 **122**/11/34 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.234-6-47.txt) Hersmunch 234/**6**/47 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.397-6-21.txt) Hersmunch 397/6/**21**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.397-6-21.txt) Hersmunch 397/**6**/21 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/33762/33762.303-6-23.txt) Hersmunch 303/**6**/23 |
|
| [SIGNAL PATTERN DETECTOR](https://zlbb.faendir.com/tis/40196) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/40196/40196.82-8-16.txt) jpgrossman **82**/8/16 | jpgrossman 119/**4**/57 | jpgrossman 173/4/**13**
|  |  | jpgrossman 173/**4**/13 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/40196/40196.155-4-14.txt) Brian142857 155/**4**/14 |
|
| [SEQUENCE PEAK DETECTOR](https://zlbb.faendir.com/tis/41427) | LowLevel\- **195**/9/49 | jpgrossman 269/**4**/35 | jpgrossman 663/4/**22**
|  |  | jpgrossman 663/**4**/22 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/41427/41427.334-4-26.txt) Hersmunch 334/**4**/26 |
|
| [SEQUENCE REVERSER](https://zlbb.faendir.com/tis/42656) | jpgrossman **190**/7/49 | LowLevel\- 252/**3**/26 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/42656/42656.314-3-8.txt) dionadar 314/3/**8**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/42656/42656.314-3-8.txt) dionadar 314/**3**/8 |
|
| [SIGNAL MULTIPLIER](https://zlbb.faendir.com/tis/43786) | jpgrossman **229**/9/79 | Brian142857 386/**4**/33 | jpgrossman 1645/4/**14**
|  |  | jpgrossman 1645/**4**/14 |
|  |  | ImprobableFish 434/**4**/23 |
|
| [IMAGE TEST PATTERN 1](https://zlbb.faendir.com/tis/50370) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.1186-2-20.txt) GuiltyBystander **1186**/2/20 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.1346-1-15.txt) GuiltyBystander 1346/**1**/15 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.2282-1-7.txt) GuiltyBystander 2282/1/**7**
|  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.1186-3-10.txt) ImprobableFish **1186**/3/10 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/50370/50370.2282-1-7.txt) GuiltyBystander 2282/**1**/7 |
|  |  | GuiltyBystander 1742/**1**/8 |
|
| [IMAGE TEST PATTERN 2](https://zlbb.faendir.com/tis/51781) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/51781/51781.1150-3-15.txt) Untellect **1150**/3/15 | MAKESPEARE 1418/**1**/15 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/51781/51781.1221-3-9.txt) rhinospray 1221/3/**9**
|  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/51781/51781.1150-4-13.txt) biggiemac42 **1150**/4/13 | MAKESPEARE 3596/**1**/9 | MAKESPEARE 3596/1/**9**
|  |  | MAKESPEARE 1778/**1**/11 |
|
| [EXPOSURE MASK VIEWER](https://zlbb.faendir.com/tis/52544) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/52544/52544.515-5-36.txt) Hersmunch **515**/5/36 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/52544/52544.549-4-58.txt) Hersmunch 549/**4**/58 | jpgrossman 1037/5/**25**
|  |  | Brian142857 601/**4**/34 |
|  |  | PocketLint2012 568/**4**/35 |
|
| [HISTOGRAM VIEWER](https://zlbb.faendir.com/tis/53897) | PocketLint2012 **1215**/11/142 | jpgrossman 1801/**4**/56 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/53897/53897.2466-6-15.txt) Grimmy 2466/6/**15**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/53897/53897.2773-4-15.txt) Hersmunch 2773/**4**/15 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/53897/53897.2773-4-15.txt) Hersmunch 2773/4/**15**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/53897/53897.2466-4-16.txt) Grimmy 2466/**4**/16 |
|
| [SIGNAL WINDOW FILTER](https://zlbb.faendir.com/tis/60099) | \_Fluff\_ **200**/7/22 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/60099/60099.313-4-19.txt) jpgrossman 313/**4**/19 | jpgrossman 392/4/**16**
|  |  | jpgrossman 392/**4**/16 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/60099/60099.314-4-18.txt) dionadar 314/**4**/18 |
|
| [SIGNAL DIVIDER](https://zlbb.faendir.com/tis/61212) | jpgrossman **880**/9/112 | jpgrossman 1492/**5**/44 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/61212/61212.4488-6-19.txt) Hersmunch 4488/6/**19**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/61212/61212.6155-5-19.txt) \_Fluff\_ 6155/**5**/19 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/61212/61212.6155-5-19.txt) \_Fluff\_ 6155/5/**19**
|  |  | jpgrossman 1824/**5**/33 |
|
| [SEQUENCE INDEXER](https://zlbb.faendir.com/tis/62711) | trevdak2 **301**/9/102 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.926-4-50.txt) Hersmunch 926/**4**/50 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.2158-5-17.txt) ImprobableFish 2158/5/**17**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.3173-4-21.txt) Hersmunch 3173/**4**/21 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/62711/62711.932-4-39.txt) Hersmunch 932/**4**/39 |
|
| [SEQUENCE SORTER](https://zlbb.faendir.com/tis/63534) | jpgrossman **468**/7/97 | jpgrossman 1398/**3**/44 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/63534/63534.1503-4-26.txt) Hersmunch 1503/4/**26**
|  |  | Hersmunch 1879/**3**/34 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/63534/63534.1541-3-35.txt) Hersmunch 1541/**3**/35 |
|
| [STORED IMAGE DECODER](https://zlbb.faendir.com/tis/70601) | jpgrossman **1190**/7/51 | jpgrossman 1371/**4**/41 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/70601/70601.2707-4-16.txt) Hersmunch 2707/4/**16**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/70601/70601.2707-4-16.txt) Hersmunch 2707/**4**/16 |
|  |  | jpgrossman 1635/**4**/21 |
|
| [UNKNOWN](https://zlbb.faendir.com/tis/UNKNOWN) | jpgrossman **247**/8/64 | jpgrossman 317/**4**/54 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/UNKNOWN/UNKNOWN.649-4-25.txt) Hersmunch 649/4/**25**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/UNKNOWN/UNKNOWN.649-4-25.txt) Hersmunch 649/**4**/25 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/UNKNOWN/UNKNOWN.392-4-32.txt) Hersmunch 392/**4**/32 |
|
| **Totals** | **9106** | **95** | **347**

# TIS-NET DIRECTORY

| Puzzle | Cycles | Nodes | Instructions
| --- | --- | --- | --- | ---
| [SEQUENCE MERGER](https://zlbb.faendir.com/tis/NEXUS.00.526.6) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.283-6-33.txt) Hersmunch **283**/6/33 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.283-6-33.txt) Hersmunch 283/**6**/33 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.370-6-22.txt) Hersmunch 370/6/**22**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.370-6-22.txt) Hersmunch 370/**6**/22 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.342-6-23.txt) Hersmunch 342/**6**/23 |
|
| [INTEGER SERIES CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.01.874.8) | jpgrossman **268**/9/135 | jpgrossman 870/**3**/35 | \_Fluff\_ 4014/3/**10**
|  |  | \_Fluff\_ 4014/**3**/10 |
|  |  | Brian142857 1389/**3**/18 |
|
| [SEQUENCE RANGE LIMITER](https://zlbb.faendir.com/tis/NEXUS.02.981.2) | jpgrossman **154**/11/99 | jpgrossman 259/**5**/44 | jpgrossman 506/5/**25**
|  |  | jpgrossman 506/**5**/25 |
|  |  | jpgrossman 310/**5**/30 |
|
| [SIGNAL ERROR CORRECTOR](https://zlbb.faendir.com/tis/NEXUS.03.176.9) | jpgrossman **140**/10/54 | jpgrossman 256/**5**/21 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.03.176.9/NEXUS.03.176.9.437-5-14.txt) ImprobableFish 437/5/**14**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.03.176.9/NEXUS.03.176.9.437-5-14.txt) ImprobableFish 437/**5**/14 |
|  |  | jpgrossman 290/**5**/16 |
|
| [SUBSEQUENCE EXTRACTOR](https://zlbb.faendir.com/tis/NEXUS.04.340.5) | Brian142857 **94**/6/29 | Brian142857 112/**4**/32 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.160-4-18.txt) \_Fluff\_ 160/4/**18**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.160-4-18.txt) \_Fluff\_ 160/**4**/18 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.04.340.5/NEXUS.04.340.5.113-4-23.txt) longingforrest 113/**4**/23 |
|
| [SIGNAL PRESCALER](https://zlbb.faendir.com/tis/NEXUS.05.647.1) | Brian142857 **304**/8/66 | Brian142857 398/**6**/50 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.05.647.1/NEXUS.05.647.1.10614-6-17.txt) jpgrossman 10614/6/**17**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.05.647.1/NEXUS.05.647.1.10614-6-17.txt) jpgrossman 10614/**6**/17 |
|  |  | Brian142857 423/**6**/41 |
|
| [SIGNAL AVERAGER](https://zlbb.faendir.com/tis/NEXUS.06.786.0) | jpgrossman **538**/10/89 | jpgrossman 1164/**4**/56 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.06.786.0/NEXUS.06.786.0.88682-4-10.txt) Hersmunch 88682/4/**10**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.06.786.0/NEXUS.06.786.0.88682-4-10.txt) Hersmunch 88682/**4**/10 |
|  |  | jpgrossman 1201/**4**/49 |
|
| [SUBMAXIMUM SELECTOR](https://zlbb.faendir.com/tis/NEXUS.07.050.0) | jpgrossman **367**/11/59 | jpgrossman 615/**6**/54 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.07.050.0/NEXUS.07.050.0.1729-10-33.txt) Hersmunch 1729/10/**33**
|  |  | jpgrossman 631/**6**/49 | jpgrossman 1755/7/**33**
|
| [DECIMAL DECOMPOSER](https://zlbb.faendir.com/tis/NEXUS.08.633.9) | jpgrossman **400**/10/104 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.08.633.9/NEXUS.08.633.9.701-5-58.txt) Hersmunch 701/**5**/58 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.08.633.9/NEXUS.08.633.9.7706-5-18.txt) Hersmunch 7706/5/**18**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.08.633.9/NEXUS.08.633.9.7706-5-18.txt) Hersmunch 7706/**5**/18 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.08.633.9/NEXUS.08.633.9.745-5-40.txt) Hersmunch 745/**5**/40 |
|
| [SEQUENCE MODE CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.09.904.9) | jpgrossman **289**/7/102 | jpgrossman 1749/**3**/44 | jpgrossman 1801/3/**42**
|  |  | jpgrossman 1801/**3**/42 |
|  |  | jpgrossman 1752/**3**/43 |
|
| [SEQUENCE NORMALIZER](https://zlbb.faendir.com/tis/NEXUS.10.656.5) | jpgrossman **399**/8/86 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.10.656.5/NEXUS.10.656.5.665-3-40.txt) Hersmunch 665/**3**/40 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.10.656.5/NEXUS.10.656.5.725-4-27.txt) Hersmunch 725/4/**27**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.10.656.5/NEXUS.10.656.5.749-3-37.txt) Hersmunch 749/**3**/37 |
|  |  | jpgrossman 674/**3**/38 |
|
| [IMAGE TEST PATTERN 3](https://zlbb.faendir.com/tis/NEXUS.11.711.2) | PocketLint2012 **882**/10/80 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.11.711.2/NEXUS.11.711.2.2574-3-40.txt) biggiemac42 2574/**3**/40 | jpgrossman 2746/5/**27**
|  |  | jpgrossman 2764/**3**/32 |
|
| [IMAGE TEST PATTERN 4](https://zlbb.faendir.com/tis/NEXUS.12.534.4) | PocketLint2012 **1166**/8/51 | MAKESPEARE 1474/**2**/24 | Brian142857 2289/3/**16**
|  |  | Brian142857 2290/**2**/17 |
|  |  | MAKESPEARE 1645/**2**/21 |
|
| [SPATIAL PATH VIEWER](https://zlbb.faendir.com/tis/NEXUS.13.370.9) | jpgrossman **745**/9/62 | jpgrossman 1050/**4**/55 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.13.370.9/NEXUS.13.370.9.1507-4-22.txt) Hersmunch 1507/4/**22**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.13.370.9/NEXUS.13.370.9.1507-4-22.txt) Hersmunch 1507/**4**/22 |
|
| [CHARACTER TERMINAL](https://zlbb.faendir.com/tis/NEXUS.14.781.3) | jpgrossman **590**/9/105 | jpgrossman 830/**4**/49 | jpgrossman 1007/6/**34**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.14.781.3/NEXUS.14.781.3.1276-4-39.txt) Hersmunch 1276/**4**/39 |
|
| [BACK-REFERENCE REIFIER](https://zlbb.faendir.com/tis/NEXUS.15.897.9) | jpgrossman **227**/9/57 | jpgrossman 369/**4**/53 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.15.897.9/NEXUS.15.897.9.849-4-21.txt) Hersmunch 849/4/**21**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.15.897.9/NEXUS.15.897.9.849-4-21.txt) Hersmunch 849/**4**/21 |
|  |  | jpgrossman 736/**4**/22 |
|
| [DYNAMIC PATTERN DETECTOR](https://zlbb.faendir.com/tis/NEXUS.16.212.8) | jpgrossman **166**/11/56 | jpgrossman 250/**5**/56 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.16.212.8/NEXUS.16.212.8.647-8-23.txt) Hersmunch 647/8/**23**
|  |  | Brian142857 253/**5**/49 |
|
| [SEQUENCE GAP INTERPOLATOR](https://zlbb.faendir.com/tis/NEXUS.17.135.0) | jpgrossman **106**/7/81 | jpgrossman 351/**3**/29 | jpgrossman 498/3/**21**
|  |  | jpgrossman 498/**3**/21 |
|
| [DECIMAL TO OCTAL CONVERTER](https://zlbb.faendir.com/tis/NEXUS.18.427.7) | jpgrossman **138**/11/105 | Brian142857 277/**4**/50 | Brian142857 955/4/**12**
|  |  | Brian142857 955/**4**/12 |
|  |  | Brian142857 287/**4**/33 |
|
| [PROLONGED SEQUENCE SORTER](https://zlbb.faendir.com/tis/NEXUS.19.762.9) | PocketLint2012 **362**/8/90 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.19.762.9/NEXUS.19.762.9.2177-3-45.txt) Hersmunch 2177/**3**/45 | jpgrossman 2576/4/**28**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.19.762.9/NEXUS.19.762.9.2581-3-37.txt) Hersmunch 2581/**3**/37 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.19.762.9/NEXUS.19.762.9.2214-3-38.txt) Hersmunch 2214/**3**/38 |
|
| [PRIME FACTOR CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.20.433.1) | jpgrossman **409**/11/147 | jpgrossman 5924/**3**/38 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.20.433.1/NEXUS.20.433.1.17826-6-26.txt) Hersmunch 17826/6/**26**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.20.433.1/NEXUS.20.433.1.10902-3-27.txt) Hersmunch 10902/**3**/27 |
|  |  | ImprobableFish 6234/**3**/34 |
|
| [SIGNAL EXPONENTIATOR](https://zlbb.faendir.com/tis/NEXUS.21.601.6) | jpgrossman **290**/8/119 | jpgrossman 1444/**4**/47 | jpgrossman 4256/4/**23**
|  |  | jpgrossman 4256/**4**/23 |
|
| [T20 NODE EMULATOR](https://zlbb.faendir.com/tis/NEXUS.22.280.8) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.168-9-49.txt) Hersmunch **168**/9/49 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.293-4-34.txt) Hersmunch 293/**4**/34 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.212-5-22.txt) ImprobableFish 212/5/**22**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.318-4-23.txt) Hersmunch 318/**4**/23 |
|
| [T31 NODE EMULATOR](https://zlbb.faendir.com/tis/NEXUS.23.727.9) | jpgrossman **166**/9/108 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.738-3-36.txt) Hersmunch 738/**3**/36 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.800-4-23.txt) Hersmunch 800/4/**23**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.874-3-31.txt) Hersmunch 874/**3**/31 |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.741-3-34.txt) Hersmunch 741/**3**/34 |
|
| [WAVE COLLAPSE SUPERVISOR](https://zlbb.faendir.com/tis/NEXUS.24.511.7) | jpgrossman **245**/12/57 | jpgrossman 428/**6**/53 | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.24.511.7/NEXUS.24.511.7.957-7-30.txt) Hersmunch 957/7/**30**
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.24.511.7/NEXUS.24.511.7.957-6-31.txt) Hersmunch 957/**6**/31 |
|  |  | jpgrossman 470/**6**/36 |
|
| **Totals** | **8896** | **102** | **564**

# Achievement Solutions

| Puzzle | Cycles | Nodes | Instructions
| --- | --- | --- | --- | ---
| [SELF-TEST DIAGNOSTIC](https://zlbb.faendir.com/tis/00150?visualizerFilterTIS-00150.modifiers.achievement=true) (BUSY_LOOP) | Brian142857 **100001**/8/12/a | Brian142857 100001/**8**/12/a | Brian142857 100001/8/**12**/a
|
| [SIGNAL COMPARATOR](https://zlbb.faendir.com/tis/21340?visualizerFilterTIS-21340.modifiers.achievement=true) (UNCONDITIONAL) | jpgrossman **210**/6/66/a | jpgrossman 210/**6**/66/a | jpgrossman 494/6/**16**/a
|  |  | jpgrossman 494/**6**/16/a |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/21340/21340.282-6-19-a.txt) Hersmunch 282/**6**/19/a |
|
| [SEQUENCE REVERSER](https://zlbb.faendir.com/tis/42656?visualizerFilterTIS-42656.modifiers.achievement=true) (NO_MEMORY) | jpgrossman **191**/7/50/a | jpgrossman 228/**4**/51/a | jpgrossman 685/5/**26**/a
|  |  | GuiltyBystander 606/**4**/40/a |
|  |  | jpgrossman 233/**4**/47/a |
|
| **Totals** | **100402** | **18** | **54**

# Cheating Solutions

| Puzzle | Cycles | Nodes | Instructions
| --- | --- | --- | --- | ---
| [SEQUENCE COUNTER](https://zlbb.faendir.com/tis/31904?visualizerFilterTIS-31904.modifiers.cheating=true) | F1000003 **55**/11/147/c |  |
|
| [SIGNAL EDGE DETECTOR](https://zlbb.faendir.com/tis/32050?visualizerFilterTIS-32050.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/32050/32050.80-9-98-c.txt) biggiemac42 **80**/9/98/c |  |
|
| [SIGNAL PATTERN DETECTOR](https://zlbb.faendir.com/tis/40196?visualizerFilterTIS-40196.modifiers.cheating=true) | AmitaiG **81**/9/56/c |  |
|
| [SEQUENCE PEAK DETECTOR](https://zlbb.faendir.com/tis/41427?visualizerFilterTIS-41427.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/41427/41427.30-11-150-c.txt) biggiemac42 **30**/11/150/c |  |
|
| [SEQUENCE REVERSER](https://zlbb.faendir.com/tis/42656?visualizerFilterTIS-42656.modifiers.cheating=true) | Amitai **185**/9/87/c |  |
|
| [SEQUENCE SORTER](https://zlbb.faendir.com/tis/63534?visualizerFilterTIS-63534.modifiers.cheating=true) |  | jpgrossman 1361/**3**/44/c |
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_100_SEGMENT_MAP/63534/63534.1481-3-35-c.txt) Hersmunch 1481/**3**/35/c |
|
| [UNKNOWN](https://zlbb.faendir.com/tis/UNKNOWN?visualizerFilterTIS-UNKNOWN.modifiers.cheating=true) |  | jpgrossman 314/**4**/51/c |
|
| [SEQUENCE MERGER](https://zlbb.faendir.com/tis/NEXUS.00.526.6?visualizerFilterTIS-NEXUS.00.526.6.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.281-6-40-c.txt) Hersmunch **281**/6/40/c | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.00.526.6/NEXUS.00.526.6.281-6-40-c.txt) Hersmunch 281/**6**/40/c |
|
| [SIGNAL AVERAGER](https://zlbb.faendir.com/tis/NEXUS.06.786.0?visualizerFilterTIS-NEXUS.06.786.0.modifiers.cheating=true) |  | jpgrossman 1124/**4**/48/c |
|
| [SEQUENCE MODE CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.09.904.9?visualizerFilterTIS-NEXUS.09.904.9.modifiers.cheating=true) | trevdak2 **24**/5/32/c | TehSeven 25/**3**/30/c | TehSeven 25/3/**30**/c
|
| [DYNAMIC PATTERN DETECTOR](https://zlbb.faendir.com/tis/NEXUS.16.212.8?visualizerFilterTIS-NEXUS.16.212.8.modifiers.cheating=true) | TehSeven **78**/6/53/c | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.16.212.8/NEXUS.16.212.8.149-3-23-c.txt) biggiemac42 149/**3**/23/c | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.16.212.8/NEXUS.16.212.8.355-5-20-c.txt) Hersmunch 355/5/**20**/c
|
| [SEQUENCE GAP INTERPOLATOR](https://zlbb.faendir.com/tis/NEXUS.17.135.0?visualizerFilterTIS-NEXUS.17.135.0.modifiers.cheating=true) | TehSeven **29**/6/70/c |  |
|
| [PROLONGED SEQUENCE SORTER](https://zlbb.faendir.com/tis/NEXUS.19.762.9?visualizerFilterTIS-NEXUS.19.762.9.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.19.762.9/NEXUS.19.762.9.210-8-99-c.txt) biggiemac42 **210**/8/99/c |  |
|
| [PRIME FACTOR CALCULATOR](https://zlbb.faendir.com/tis/NEXUS.20.433.1?visualizerFilterTIS-NEXUS.20.433.1.modifiers.cheating=true) |  |  | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.20.433.1/NEXUS.20.433.1.15666-6-26-c.txt) Hersmunch 15666/6/**26**/c
|
| [SIGNAL EXPONENTIATOR](https://zlbb.faendir.com/tis/NEXUS.21.601.6?visualizerFilterTIS-NEXUS.21.601.6.modifiers.cheating=true) | jpgrossman **278**/8/116/c |  |
|
| [T20 NODE EMULATOR](https://zlbb.faendir.com/tis/NEXUS.22.280.8?visualizerFilterTIS-NEXUS.22.280.8.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.29-4-36-c.txt) biggiemac42 **29**/4/36/c | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.22.280.8/NEXUS.22.280.8.40-3-34-c.txt) biggiemac42 40/**3**/34/c |
|
| [T31 NODE EMULATOR](https://zlbb.faendir.com/tis/NEXUS.23.727.9?visualizerFilterTIS-NEXUS.23.727.9.modifiers.cheating=true) | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.21-3-26-c.txt) Hersmunch **21**/3/26/c | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.21-3-26-c.txt) Hersmunch 21/**3**/26/c | [📄](https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master/TIS_NET_DIRECTORY/NEXUS.23.727.9/NEXUS.23.727.9.661-4-23-c.txt) Hersmunch 661/4/**23**/c
|
| **Totals** | **1381** | **29** | **99**

# Most record solutions

| Solutions | Name(s)
| --- | --- 
| 89 | jpgrossman
| 56 | Hersmunch
| 21 | Brian142857
| 9 | ImprobableFish
| 8 | biggiemac42
| 6 | GuiltyBystander
| 5 | MAKESPEARE, PocketLint2012
| 4 | \_Fluff\_, Community
| 3 | dionadar, TehSeven
| 2 | Grimmy, LowLevel\-, trevdak2
| 1 | Amitai, AmitaiG, F1000003, Ling Ling, longingforrest, pusalieth, rhinospray, Snowball, Untellect

# Most frontier solutions

| Solutions | Name(s)
| --- | --- 
| 214 | Hersmunch
| 119 | jpgrossman
| 113 | Brian142857
| 32 | GuiltyBystander
| 27 | ImprobableFish
| 25 | Csaboka
| 18 | biggiemac42
| 17 | longingforrest
| 12 | Andrash
| 11 | Grimmy
| 6 | MAKESPEARE, ShadowCluster
| 5 | \_Fluff\_, Community, PocketLint2012
| 4 | Snowball
| 3 | dionadar, i\_dont\_want\_karma, rhinospray, TehSeven
| 2 | LowLevel\-, trevdak2, vcque
| 1 | 12345ieee, Akari\_Takai, Amitai, AmitaiG, andy75381, bdekeijzer, Daniel\_Nson, Dariush, Entity\_, F1000003, Forgetful, isaac\.wass, killerbee13, l10veu, Ling Ling, maka\_RTH, mlehmk, mr\_screg, proudHaskeller, pusalieth, shufb\_demon, Tux1, Untellect
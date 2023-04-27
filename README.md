# CadregaBot

An implementation for ConnectX AI for the course of Algorithms and Data Structures at the University of Bologna A.A. 2022/2023.

Originally developed as an AI for MNKGame for the project of the prior year.

> **Important note!**  
> 
> Currently, the bot isn't very smart on ConnectX.  
> This is due to the heuristic not being made for ConnectX, so the results are suboptimal.  
> Also, sometimes the selected move happens to be illegal (i.e. the selected column is already full).  
> I don't really have the time to look into these issues. If anyone would like to fix it, any contribution is appreciated!

Compile and run:

```txt
cd src

javac */*/*.java */*.java

java connectx.CXGame M N X connectx.cadregaBot.CadregaBot
```

Values for M, N and X:
- 6 7 4 is the classic Connect 4
- 5 5 3 is a board 5x5 where players must make lines of length 3
- 10 7 5 is a board 10x7 where players must make lines of length 5
- etc etc


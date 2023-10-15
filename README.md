# Chessout: A Blockchain-Powered Chess Tournament Platform

## How It Started
Chessout began as a personal project during the COVID-19 pandemic. After being laid off, I invested my time in creating a solution to address the frustrations faced by chess-loving parents and tournament organizers. The key pain points were:
- Lack of notifications when kids finish a chess game.
- Difficulty in organizing casual tournaments.

I shared this project with tournament organizers, and the response was overwhelming. Parents and tournament organizers loved it. Today, it's used in major tournaments in Belgium, and I plan to expand its reach across Europe after the hackathon.

## What Is Chessout
Chessout is a blockchain-powered platform designed for everyone involved in chess tournaments:
- Parents and supporters receive real-time updates and push notifications.
- Chess players benefit from streamlined tournament organization.
- Tournament organizers find it easier to manage logistics.
- Tournament arbiters gain tools for efficient decision-making.
- Catering services can offer event-specific menus.
- Casual coffee or beer drinkers can enjoy their beverages hassle-free.

## MultiversX Business Case
Chessout simplifies tournament logistics through blockchain technology. Key features include:
- Utilization of smart contracts to handle financial transactions, such as tournament entry fees and prize distribution.
- Defining event-specific menus for catering services through blockchain.
- Facilitating payments for refreshments, like coffee and beer.
- Earning revenue through small percentage fees on blockchain transactions.

## Future MultiversX Ideas
To encourage chess enthusiasts to play more and build stronger communities, we plan to implement a staking mechanism:
- Clubs processing a specific fund threshold will enjoy increased yields for a limited time.
- This mechanism fosters real-world social interaction and positive community building.
- Players are incentivized to play more chess, leading to a happier and more engaged community.

Chessout leverages blockchain technology to enhance transparency, security, and efficiency in chess tournaments, revolutionizing the way we organize and enjoy chess events.

## Conclusion
Chessout is more than a chess tournament platform; it's a blockchain-driven solution that simplifies logistics, increases engagement, and fosters stronger chess communities. We invite developers at the hackathon to explore its blockchain features and be a part of this exciting journey.

## Mono repo structure
- chessout-app: this is the web app that we build from zero in only 2 weeks for the hackaton
- chessout-contract: the smart contract
- chessout-android: this is the android kotlin based app. We would love fi multiversX would provide support for native aplciations to integrate with xPortal. 
- chessout-shared: is the comon libraries used ty kotlin and backend. We plan to add also the backend here once we clean up some code. 

## some other things

I plan to create a 2 minutes introduction video once I get the hacathon code complete. 

Some youtube related videos to chessout can be located here
- [chessout youtube](https://www.youtube.com/@chessout3011)

In case you want to se a more rudimetary version of this file
```
git checkout 47b524be755b69eb9625599820acf96fb29c6156
```

## techical documentation
- chessout relies on reatime database to store most of the data. (clubs, players, managers, rounds, games etc.)
- You can convert a normal tournament into a multiversX tournament. 
- there are tests that cover all contract functionality and use cases. 
- you can run unlimited tournaments at the same time and the contract keeps track of all the funds, participants, winners, and prizes separate. 
- datamodel used to manage all that is quite elegand and is inspired by lotery template but improved. 

## multiversX chessout contract usage
- call: createTournament to create a new tournament. This also makrs the current wallet as manager wallet on that rounament
- call: joinTournament to join as a participant in a tournament. You will have to pay the specified join price and the contract updates the tournament available funds and add the participant to the tournaent
- call: addTounamentWinner This can only be called by the tournament manager and is use in order to set the winners and the desired prize ammount. 
- call: distribureTournamentRewords This can also be called only by tournament manager and it sends the specified rewords at the previous step to all the winners. It also takes a fee from the specified ammounts and if the entire transferd funds are higher then availe tournament funds the it will fail

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

# Technical Documentation

- **Data Storage**: Chessout relies on a real-time database to store most of its data, including information about clubs, players, managers, rounds, and games.

- **Multiverse Transformation**: You have the ability to convert a regular tournament into a multiverseX tournament.

- **Test Coverage**: The contract includes comprehensive tests that cover all contract functionality and use cases.

- **Multi-Tournament Support**: You can run an unlimited number of tournaments simultaneously, and the contract effectively manages funds, participants, winners, and prizes separately.

- **Elegant Data Model**: The data model used for managing this information is inspired by a lottery template but has been improved for efficiency and elegance.

# MultiverseX Chessout Contract Usage

- **Create Tournament**: To create a new tournament, call the `createTournament` method. This action also designates the current wallet as the manager for that tournament.

- **Join Tournament**: Use the `joinTournament` method to participate in a tournament. You'll need to pay the specified entry fee, and the contract will update the tournament's available funds while adding you as a participant.

- **Add Tournament Winners**: The `addTournamentWinner` method can only be called by the tournament manager. It is used to specify the winners and their respective prize amounts.

- **Distribute Tournament Rewards**: The `distributeTournamentRewards` method is also exclusively available to the tournament manager. It distributes the specified rewards from the previous step to all the winners. Additionally, it deducts a fee from the prize amounts. If the total transferred funds exceed the available tournament funds, the transaction will fail.


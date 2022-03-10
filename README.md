# Distributed Hash Table using Chord Protocol
## Date - `03/09/2022`
### Jaidev Mirchandani (jmirchandani@scu.edu, W1610360). 
### Hsuan Wang (hwang8@scu.edu, W1607849). 
### Tony Mathen (tmathen@scu.edu, W1629791). 
## Description 
`We have implemented a highly availbale and scalable distributed hash table using Chord protocol and implemented schemes for increased performance, replication and cosistancy. `
## Usage
#### Intialize a Chord ring with a single node
```
java Chord <Node Name>
```
#### Insert a node to an existing Chord ring
```
java Chord <New Node> <Existing Node Name> <IP Address> <Port No of the Existing Node>
```
### Example:
```
java Chord Node1 // Create a chord ring with one node, Default Port: 1099
java Chord Node2 Node1 172.31.185.2 1099 // Create a new node (Node2) and add to the existing ring
```

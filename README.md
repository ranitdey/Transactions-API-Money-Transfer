# Transfer Money Application

##About
A RESTful API implementation which allows transfer money from one Bank Account to another in any 
currency (As of now only USD and EUR is supported).

Entities which this application uses:
* _Transaction_ - This gets initialized when money transfer gets invoked.
* _Account_ - This represents the bank account of an user with all the details required.

Currency conversion:

As of now the currency conversion happens in a fixed rate.But ideally it should call a different 
service which will give the live exchange rate fir the conversion.
And also right now the implementation only supports US and EUR.More currencies can be added to it.

In memory database:

As of now I have used two concurrent hash map for storing the bank accounts and transactions.
Concurrent hash map ensures thread safety for all its operations.Right now this in memory 
implementation is not persistent. But later in time this could be replaced with an persistent 
database.

Transaction flow:
* Whenever a transaction request comes it do not get executed on the first place.First it gets added 
to the transaction list. So that asynchronous transaction executor service could pick it up and not block the 
main thread.
* Transaction executor picks up the transaction and executes the transaction and sets the 
transaction status accordingly (Successful or Failed)

Now the plus point of this approach is whenever the transaction is initiated it gets added to the 
list and user gets a response that transaction got initiated. This frees up the main thread which 
enables high concurrency.

Embedded server:

This application uses grizzly embedded server.

 
## Requirement
* Java 8
* Maven

## Running the app

First the app needs to be cloned from git.Then running the below command will install the 
dependencies and run all the tests.

    mvn clean install

To run the app execute:

    java -jar target/transactions-1.0-SNAPSHOT.jar  

The application will start on the `localhost` and will be listening to the port `8080`

## API Definition

### User Bank Account
The bank account entity in user level.

#### Structure
    {
        "id": <string>,
        "ownerName": <string>,
        "balance": <BigDecimal>,
        "blockedAmount": <BigDecimal>,
        "currency": <string - one from "USD", "EUR">
    }

#### Create Bank Account

The following creates bank account and returns the created entity with `ID` specified

    POST /accounts
    {
        "ownerName": "Ranit Dey",
        "balance": 1300,
        "blockedAmount": 0,
        "currency": "USD"
    }

Example response:

    HTTP 201 CREATED
    POST /accounts
    {
        "id": "44ebe431-d966-441a-aed7-dcaf3d3d3d44",
        "ownerName": "Ranit Dey",
        "balance": 1300,
        "blockedAmount": 0,
        "currency": "USD"
    }

#### Get Bank Account details

The following gets the particular account if it exists.

    GET /accounts/44ebe431-d966-441a-aed7-dcaf3d3d3d44

Example response:

    HTTP 200 OK
    {
        "id": "44ebe431-d966-441a-aed7-dcaf3d3d3d44",
        "ownerName": "Ranit Dey",
        "balance": 1300,
        "blockedAmount": 0,
        "currency": "USD"
    }


        
### Transaction
Transaction is initialized when money transfer is requested. Once the transaction request is created
it will be executed automatically by an executor service asynchronously.If transaction can not be 
created by some reason the Error will be returned. 

#### Structure
    {
        "id": <Integer>,
        "fromBankAccountId": <string>,
        "toBankAccountId": <string>,
        "amount": <BigDecimal>,
        "currency": <string - one from "USD", "EUR">,
        "creationDate": <timestamp>,
        "updateDate": <timestamp>,
        "status": <string - one from "STARTED", "FAILED", "SUCCEED">,
        "failMessage": <string>
    }
    
#### Create a transaction

The following creates a new transaction if possible (valid Bank Accounts and other details
should be provided). `id`, `creationDate`, `updateDate` or `status` will be updated and added
automatically. 

    POST /transactions
    {
        "fromBankAccountId": "44ebe431-d966-441a-aed7-dcaf3d3d3d44",
        "toBankAccountId": "7c3b4716-9ff9-401b-a76a-5b0c8730219b",
        "amount": 16,
        "currency": "EUR"
    }
    
Example response:

    HTTP 201 CREATED
    {
        "id": 1,
        "fromBankAccountId": "44ebe431-d966-441a-aed7-dcaf3d3d3d44",
        "toBankAccountId": "7c3b4716-9ff9-401b-a76a-5b0c8730219b",
        "amount": 16,
        "currency": "EUR",
        "creationDate": 1566538441,
        "updateDate": 1566538448,
        "status": "STARTED",
        "failMessage": ""
    }

#### Get all transactions

    GET /transactions

Example response:

    HTTP 200 OK    
    [{
             "id": 1,
             "fromBankAccountId": "44ebe431-d966-441a-aed7-dcaf3d3d3d44",
             "toBankAccountId": "7c3b4716-9ff9-401b-a76a-5b0c8730219b",
             "amount": 16,
             "currency": "EUR",
             "creationDate": 1566538441,
             "updateDate": 1566538448,
             "status": "SUCCEED",
             "failMessage": ""
     }]
      
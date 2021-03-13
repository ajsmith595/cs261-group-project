# CS261 Group Project

Group 29 - project for CS261

## Setup instructions

### Database

Firstly, you need to have MongoDB running any version between 2.6 and 4.2 inclusive (check the compatibility listings below if you are unsure), running locally on port 27017, with access control disabled. The address of database host can be modified in the `src/main/resources/server.properties` file. Simply change the `databaseHost` variable to whatever address your database is hosted at - by default this is `localhost:27017`.

The project uses the MongoDB Java Driver version 3.12; compatibility listings are here: https://docs.mongodb.com/drivers/java/#compatibility

### Java

Java version 11 is required, as well as Maven to build and run the project.

### NPM/Node

To build the client side of the application, you will also need `npm`/`node` installed.

### Building and running the application

You will first need around 750MB of storage available for the whole project, excluding the storage used for the database.
To build the application, run the `build_all.sh` script. When running the first time, it will take some time to download all `npm` and Maven packages; please be patient.

To start the project, you can use the `run_server.sh` script. This will start the server running on port 4567. Therefore, if you go to `http://localhost:4567` in your browser, you will be able to access the site.

The port the server runs on can be changed in the `src/main/resources/server.properties` file. Recompilation should not be required after making a change to this.

## Architecture

### Client

The client is a React application which interacts with the server via `fetch` requests to the API. `App.jsx` is the main file which gets loaded upon startup. We then use `react-router` to determine what component to show depending on what the URL of the current page is.

#### Development

Whilst testing/editing the files, it is often useful to be able to, in real time, edit the files, and see immediate results. To achieve this behaviour, first change the `REACT_APP_HTTP_ADDRESS` and `REACT_APP_WS_ADDRESS` variables in `client/.env.development` to use the correct port on which you are running the server. By default, these are set to the server's default port, 4567.
Once this has been done, `cd client`, and run `npm start` to start a development server. This will automatically open a development server where the page will be automatically reloaded when a change is made. Please note that this will not automatically redirect you to the `/login` page if you are not authenticated.

When you want to update the server to use the new client changes, you need to first build the client (use the `build_client.sh` script), then you need to compile the server to use the new changes (`build_server.sh`). These can both be done automatically via the `build_all.sh` script.

### Server

The server is a Java application using the Spark framework.

#### Development

If you only modify the server, the only script you need to run is `build_server.sh`. This will only recompile the server, and not the client.

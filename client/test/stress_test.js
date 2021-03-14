const {
    SERVER_HOST,
    getWebSocketPromise,
    sleep,
    sleepMs,
} = require("./common");
const fetch = require("node-fetch");
const { performance } = require("perf_hooks");

// to run this, use
// node stress_test.js NUMBER_OF_EVENTS [FEEDBACK_PER_MINUTE] [DURATION_IN_MINS]

let args = process.argv.slice(2);

if (args.length == 0) {
    console.log("Please supply number of events to create");
    process.exit(9);
}
let numberOfEvents = parseInt(args[0]);
let feedbackPerMinute = parseInt(args[1]) || 11;
let duration = parseInt(args[2]) || 5;
stressTest(numberOfEvents, feedbackPerMinute, duration);
async function stressTest(numberOfEvents, feedbackPerMinute, duration) {
    console.log(
        `Configuration:\n\tNumber of events: ${numberOfEvents}\n\tFeedback per minute: ${feedbackPerMinute}\n\tDuration: ${duration} minutes`
    );
    try {
        // Make sure the server's alive!
        let checkRequest = await fetch(`${SERVER_HOST}`);
        if (!checkRequest.ok) {
            console.log("Server not open!");
            return;
        }
    } catch (e) {
        console.log("Server not open!");
        return;
    }

    let users = [];
    let batches = duration * feedbackPerMinute;
    let feedbackPerBatch = 1;
    let numberOfUsers = numberOfEvents + feedbackPerBatch * batches;

    process.stdout.write(`Generating ${numberOfUsers} users...\r`);
    let randNum = Math.floor(100000 * Math.random());
    for (let i = 0; i < numberOfUsers; i++) {
        let username = `${randNum}u${i}`;
        let email = `${randNum}_${i}@stresstest.com`;
        users.push({
            promise: fetch(`${SERVER_HOST}/api/register`, {
                method: "POST",
                body: JSON.stringify({
                    username,
                    email,
                }),
            }),
            username,
            email,
        });
    }
    let ok = true;
    for (let user of users) {
        let response = await user.promise;
        if (!response.ok) {
            console.log(`\nGenerating user ${user.username} failed (NOT OK)`);
            ok = false;
            continue;
        }
        let json = await response.json();
        if (json.status !== "success") {
            console.log(
                `\nGenerating user ${user.username} failed (NOT SUCCESS) (${json.message})`
            );
            ok = false;
            continue;
        }
        user.cookie = response.headers.get("set-cookie");
        user.promises = [];
    }
    if (!ok) process.exit(5);

    console.log(`Generating ${numberOfUsers} users...done`);

    console.log(`Starting stress test`);

    let globalUsers = users.splice(0, feedbackPerBatch * batches);

    let events = [];
    for (let i = 0; i < numberOfEvents; i++) {
        let host = users.splice(0, 1)[0];
        let data = getWebSocketPromise(host, [
            {
                type: "open",
                title: "Test Question 1",
            },
        ]);
        events.push({
            promise: data,
            host,
        });
    }

    for (let event of events) {
        let { promise, token, eventCode } = (await event.promise).data;
        let ws;
        try {
            ws = await promise;
        } catch (err) {
            print("WS Connection failed");
            continue;
        }
        event.webSocketData = null;
        ws.on("message", (data) => {
            event.webSocketData = JSON.parse(data);
        });
        ws.send(token);
        event.eventCode = eventCode;
        event.ws = ws;
    }

    let interval = 60 / feedbackPerMinute;
    let startTime = performance.now();

    for (let i = 0; i < batches; i++) {
        console.log(`Batch ${i + 1} start`);
        let currentTime = performance.now();
        let timeElapsed = (currentTime - startTime) / 1000;

        if (timeElapsed < i * interval) {
            console.log(
                "Waiting..." + (i * interval - timeElapsed).toString() + "s"
            );
            await sleep(i * interval - timeElapsed);
        }

        let usersToUse = globalUsers.slice(
            i * feedbackPerBatch,
            (i + 1) * feedbackPerBatch
        );

        for (let event of events) {
            for (let user of usersToUse) {
                user.promises.push(
                    fetch(
                        `${SERVER_HOST}/api/event/${event.eventCode}/feedback`,
                        {
                            method: "POST",
                            headers: {
                                cookie: user.cookie,
                            },
                            body: JSON.stringify({
                                anonymous: false,
                                responses: [
                                    {
                                        questionID: "0",
                                        response: "Test response",
                                    },
                                ],
                            }),
                        }
                    )
                );
            }
            await sleepMs(5);
        }
        for (let user of usersToUse) {
            for (let promise of user.promises) {
                let response = await promise;
                if (!response.ok) {
                    console.log(`Response not ok ${user.username}`);
                } else {
                    let json = await response.json();
                    if (json.status !== "success") {
                        console.log(`JSON not success! ${user.username}`);
                    }
                }
            }
        }
        console.log(`Batch ${i + 1} complete`);
    }

    let currentTime = performance.now();
    let timeElapsed = (currentTime - startTime) / 1000;
    let avgFeedbackPerMinute = (60 * batches) / timeElapsed;

    await sleep(3);
    console.log(events);
    let numWithNoResponse = 0;
    for (let event of events) {
        if (
            !event.webSocketData ||
            event.webSocketData.totalResponses != feedbackPerBatch * batches
        ) {
            numWithNoResponse++;
        }
    }
    console.log(
        "Number of events with no response after 3 seconds: " +
            numWithNoResponse
    );
    if (numWithNoResponse == 0) {
        console.log("STRESS TEST PASS");
    } else {
        console.log("STRESS TEST FAIL");
    }
    await sleep(3);
    numWithNoResponse = 0;
    for (let event of events) {
        if (
            !event.webSocketData ||
            event.webSocketData.totalResponses != feedbackPerBatch * batches
        ) {
            numWithNoResponse++;
        }
    }
    console.log(
        "Number of events with no response after 6 seconds: " +
            numWithNoResponse
    );
    await sleep(3);
    numWithNoResponse = 0;
    for (let event of events) {
        if (
            !event.webSocketData ||
            event.webSocketData.totalResponses != feedbackPerBatch * batches
        ) {
            numWithNoResponse++;
        }
    }
    console.log(
        "Number of events with no response after 9 seconds: " +
            numWithNoResponse
    );
    await sleep(3);
    numWithNoResponse = 0;
    for (let event of events) {
        if (
            !event.webSocketData ||
            event.webSocketData.totalResponses != feedbackPerBatch * batches
        ) {
            numWithNoResponse++;
        }
        event.ws.close();
    }
    console.log(
        "Number of events with no response after 12 seconds: " +
            numWithNoResponse
    );
    console.log("Average feedback / minute: " + avgFeedbackPerMinute);
    console.log("Stress test complete!");
}

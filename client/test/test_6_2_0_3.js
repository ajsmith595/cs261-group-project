const fetch = require("node-fetch");
const { SERVER_HOST, sleep, sleepMs } = require("./common");
const AbortController = require("abort-controller");
const { performance } = require("perf_hooks");

/**
 * Checks to see that the 1 response / minute restriction is followed
 * @param {Array} users
 */
module.exports = async function test_6_2_0_3(users) {
    let host = users[0];

    let eventStartTime = new Date().getTime() + 1000 * 60 * 5; // Time + 5 mins

    let requests = [];
    for (let j = 0; j < 50; j++) {
        requests.push(
            new Promise((resolve, reject) => {
                const controller = new AbortController();
                const signal = controller.signal;
                let call = fetch(`${SERVER_HOST}/api/events`, {
                    signal,
                    method: "POST",
                    headers: {
                        cookie: host.cookie,
                    },
                    body: JSON.stringify({
                        title: "Test Event",
                        startTime: eventStartTime,
                        duration: 60,
                        questions: [
                            {
                                type: "open",
                                title: "Test Question 1",
                            },
                        ],
                    }),
                });
                call.then((e) => resolve(e));
                call.catch((e) => reject(e));
            })
        );
    }
    let eventCodes = [];
    for (let req of requests) {
        try {
            eventCodes.push((await (await req).json()).data.eventCode);
        } catch (e) {}
    }

    let randomEventCode =
        eventCodes[Math.floor(Math.random() * eventCodes.length)];

    let t0 = performance.now();
    let response = await (
        await fetch(`${SERVER_HOST}/api/event/${randomEventCode}`, {
            headers: {
                cookie: host.cookie,
            },
        })
    ).json();
    if (response.status != "success") {
        return {
            ok: false,
            message: "Event GET failed",
        };
    }
    t1 = performance.now();
    let duration20 = t1 - t0; // in second

    // Add 100,000 events
    let eventCode = "";
    for (let i = 0; i < 2000; i++) {
        requests = [];
        for (let j = 0; j < 50; j++) {
            requests.push(
                new Promise((resolve, reject) => {
                    const controller = new AbortController();
                    const signal = controller.signal;
                    let call = fetch(`${SERVER_HOST}/api/events`, {
                        signal,
                        method: "POST",
                        headers: {
                            cookie: host.cookie,
                        },
                        body: JSON.stringify({
                            title: "Test Event",
                            startTime: eventStartTime,
                            duration: 60,
                            questions: [
                                {
                                    type: "open",
                                    title: "Test Question 1",
                                },
                            ],
                        }),
                    });
                    call.then((e) => resolve(e));
                    call.catch((e) => reject(e));
                    setTimeout(() => {
                        controller.abort();
                        reject("Timeout");
                    }, 10000);
                })
            );
        }

        let useEventCode = true;
        for (let req of requests) {
            try {
                if (i == 1000 && useEventCode) {
                    eventCode = (await (await req).json()).data.eventCode;
                    useEventCode = false;
                } else {
                    await req;
                }
            } catch (e) {}
        }
    }
    await sleep(2); // Wait 2 seconds for the server to handle all the event adds.
    t0 = performance.now();
    response = await (
        await fetch(`${SERVER_HOST}/api/event/${eventCode}`, {
            headers: {
                cookie: host.cookie,
            },
        })
    ).json();
    if (response.status != "success") {
        return {
            ok: false,
            message: "Event GET failed",
        };
    }
    t1 = performance.now();
    let duration100000 = t1 - t0;
    if ((duration100000 - duration20) / 1000 > 0.1) {
        return {
            ok: false,
            message:
                "100,000 event lookup took more than 0.1 seconds longer than 20: " +
                (duration100000 - duration20) / 1000,
        };
    }
    return {
        ok: true,
    };
};

module.exports.usersRequired = 1;
module.exports.timeRequired = 60;
module.exports.description = "Mass event lookup";
module.exports.after = true; // Tells the tester to do this test after everything else, since it's quite intensive

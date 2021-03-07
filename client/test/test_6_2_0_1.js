const fetch = require("node-fetch");
const {
    SERVER_HOST,
    sleep,
    sleepMs,
    getWebSocketPromise,
} = require("./common");
const AbortController = require("abort-controller");

/**
 * Creates 100 users which give feedback on an event at once. Test checks to see host data is given back within 3 seconds.
 * @param {Array} users
 */
module.exports = async function test_6_2_0_1(users) {
    let host = users.splice(0, 1)[0];

    let data = await getWebSocketPromise(host, [
        {
            type: "open",
            title: "Test Question 1",
        },
    ]);
    if (!data.ok) {
        return data;
    }
    let { promise, token, eventCode } = data.data;
    let ws;
    try {
        ws = await promise;
    } catch (err) {
        return {
            ok: false,
            message: "WebSocket connection failed",
        };
    }

    let wsData = null;
    ws.on("message", (data) => {
        wsData = JSON.parse(data);
    });
    ws.send(token);

    await sleepMs(200); // wait 200ms

    for (let user of users) {
        user.request = fetch(`${SERVER_HOST}/api/event/${eventCode}/feedback`, {
            method: "POST",
            body: JSON.stringify({
                anonymous: false,
                responses: [{ questionID: "0", response: "Test Response" }],
            }),
            headers: {
                cookie: user.cookie,
            },
        });
        await sleepMs(20);
    }
    let failure = null;
    for (let user of users) {
        let json = await (await user.request).json();
        if (json.status !== "success") {
            failure = {
                ok: false,
                message: "Feedback request failed",
            };
        }
    }
    if (failure != null) {
        ws.close();
        return failure;
    }
    await sleep(3);
    ws.close();
    if (wsData == null) {
        return {
            ok: false,
            message: `No WebSocket data received (${eventCode})`,
        };
    }
    if (wsData.totalResponses < users.length) {
        return {
            ok: false,
            message: `${users.length} responses were sent but only ${wsData.totalResponses} have been detected in the 3 seconds given (${eventCode})`,
        };
    }
    return {
        ok: true,
    };
};

module.exports.usersRequired = 101;
module.exports.timeRequired = 35;
module.exports.description = "Mass feedback delay";

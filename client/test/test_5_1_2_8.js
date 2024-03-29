const fetch = require("node-fetch");
const {
    SERVER_HOST,
    sleep,
    getWebSocketPromise,
    sleepMs,
} = require("./common");
/**
 * Giving feedback, checking the mood is positive given 5 attendees all give positive comments.
 * @param {Array} users
 */
module.exports = async function test_5_1_2_8(users) {
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

    let body = JSON.stringify({
        anonymous: false,
        responses: [{ questionID: "0", response: "This is really great!" }],
    });
    for (let user of users) {
        user.request = fetch(`${SERVER_HOST}/api/event/${eventCode}/feedback`, {
            method: "POST",
            body,
            headers: {
                cookie: user.cookie,
            },
        });
    }
    let failure = null;
    for (let user of users) {
        let json = await (await user.request).json();
        if (json.status != "success") {
            failure = {
                ok: false,
                message: "Could not properly give feedback; request failed",
            };
        }
    }
    if (failure) {
        await ws.close();
        return failure;
    }

    await sleep(3); // Wait 3 seconds for new data
    if (wsData == null) {
        return {
            ok: false,
            message: `WebSocket data was not supplied within 3 seconds (${eventCode})`,
        };
    }
    if (wsData.questions[0].currentMood <= 0) {
        return {
            ok: false,
            message: `A negative mood was obtained with positive messages (${eventCode})`,
        };
    }
    wsData = null;

    await sleep(60); // wait for feedback timeout

    body = JSON.stringify({
        anonymous: false,
        responses: [{ questionID: "0", response: "This is terrible!" }],
    });
    for (let user of users) {
        user.request = fetch(`${SERVER_HOST}/api/event/${eventCode}/feedback`, {
            method: "POST",
            body,
            headers: {
                cookie: user.cookie,
            },
        });
    }
    failure = null;
    for (let user of users) {
        let json = await (await user.request).json();
        if (json.status != "success") {
            failure = {
                ok: false,
                message: "Could not properly give feedback; request failed",
            };
        }
    }
    if (failure) {
        await ws.close();
        return failure;
    }
    await sleep(3); // Wait 3 seconds for new data
    await ws.close();
    if (wsData == null) {
        return {
            ok: false,
            message: `WebSocket data was not supplied within 3 seconds (${eventCode})`,
        };
    }
    if (wsData.questions[0].currentMood >= 0) {
        return {
            ok: false,
            message: `A positive mood was obtained with negative messages (${eventCode})`,
        };
    }

    return {
        ok: true,
    };
};

module.exports.usersRequired = 6;
module.exports.timeRequired = 65;
module.exports.description = "Correct mood checking";

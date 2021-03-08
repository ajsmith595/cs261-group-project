const fetch = require("node-fetch");
const {
    SERVER_HOST,
    sleep,
    getWebSocketPromise,
    sleepMs,
} = require("./common");
/**
 * Check the numeric view of the host view correctly gives the rolling average. 1 host, 5 attendees. Attendees give result, then
 * @param {Array} users
 */
module.exports = async function test_5_1_2_10(users) {
    let host = users.splice(0, 1)[0];
    let data = await getWebSocketPromise(host, [
        {
            type: "numeric",
            title: "Test Question 1",
            min: 0,
            max: 10,
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

    let total1 = 0;
    for (let user of users) {
        let val = Math.floor(Math.random() * 11);
        total1 += val;
        user.request = fetch(`${SERVER_HOST}/api/event/${eventCode}/feedback`, {
            method: "POST",
            body: JSON.stringify({
                anonymous: false,
                responses: [{ questionID: "0", response: val }],
            }),
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

    await sleep(5 * 60);
    let total2 = 0;
    for (let user of users) {
        let val = Math.floor(Math.random() * 11);
        total2 += val;
        user.request = fetch(`${SERVER_HOST}/api/event/${eventCode}/feedback`, {
            method: "POST",
            body: JSON.stringify({
                anonymous: false,
                responses: [{ questionID: "0", response: val }],
            }),
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

    let points = wsData.questions[0].points;
    if (points.length < 2) {
        return {
            ok: false,
            message: "Not enough points for graph!",
        };
    }
    let last2 = points.slice(-2);

    let average1 = total1 / 5;
    let average2 = total2 / 5;

    if (
        Math.round(last2[0].value * 10) != Math.round(average1 * 10) ||
        Math.round(last2[1].value * 10) != Math.round(average2 * 10)
    ) {
        return {
            ok: false,
            message: "Incorrect points",
        };
    }
    return {
        ok: true,
    };
};
module.exports.usersRequired = 6;
module.exports.timeRequired = 5 * 61; // 5 mins between rolling average, plus 5 seconds for other delays
module.exports.description = "Correct rolling average";

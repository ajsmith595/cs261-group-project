const fetch = require("node-fetch");
const {
    SERVER_HOST,
    sleep,
    getWebSocketPromise,
    sleepMs,
} = require("./common");
/**
 * Giving feedback from 5 attendees, making sure the numerical average is calculated correctly.
 * @param {Array} users
 */
module.exports = async function test_5_1_2_9(users) {
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

    await sleepMs(200); // wait 200ms

    let total = 0;
    let totalNum = 0;
    for (let user of users) {
        let random = Math.floor(Math.random() * 11); // 0 - 10
        totalNum++;
        total += random;
        user.request = fetch(`${SERVER_HOST}/api/event/${eventCode}/feedback`, {
            method: "POST",
            body: JSON.stringify({
                anonymous: false,
                responses: [{ questionID: "0", response: random }],
            }),
            headers: {
                cookie: user.cookie,
            },
        });
        await sleepMs(20); // Wait between requests, since the server struggles a little
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
    await ws.close();
    if (wsData == null) {
        return {
            ok: false,
            message: `WebSocket data was not received (${eventCode})`,
        };
    }
    let average = wsData.questions[0].stats.overallAverage;
    average = Math.round(average * 10) / 10;

    let actualAverage = total / totalNum;
    actualAverage = Math.round(actualAverage * 10) / 10;

    if (actualAverage != average) {
        return {
            ok: false,
            message: `The overall average value is incorrect; ${average} was returned, but the actual average is ${actualAverage} (${eventCode})`,
        };
    }
    return {
        ok: true,
    };
};

module.exports.usersRequired = 6;
module.exports.timeRequired = 5;
module.exports.description = "Correct average calculated";

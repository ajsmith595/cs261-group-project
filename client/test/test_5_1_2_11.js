const fetch = require("node-fetch");
const {
    SERVER_HOST,
    sleep,
    getWebSocketPromise,
    sleepMs,
} = require("./common");
/**
 * Check the choice questions give correct feedback
 * @param {Array} users
 */
module.exports = async function test_5_1_2_11(users) {
    let host = users.splice(0, 1)[0];
    let data = await getWebSocketPromise(host, [
        {
            type: "choice",
            title: "Test Question 1",
            allowMultiple: false,
            choices: ["Option 1", "Option 2", "Option 3"],
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

    let choicesChosen = [0, 0, 0];
    for (let user of users) {
        let val = Math.floor(Math.random() * 3);
        choicesChosen[val]++;
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

    await sleep(3); // Wait 3 seconds for new data
    await ws.close();

    if (wsData == null) {
        return {
            ok: false,
            message: `WebSocket data was not supplied within 3 seconds (${eventCode})`,
        };
    }
    let options = wsData.questions[0].options;
    if (
        options[0].number != choicesChosen[0] ||
        options[1].number != choicesChosen[1] ||
        options[2].number != choicesChosen[2]
    ) {
        return {
            ok: false,
            message: "Incorrect values for bar chart",
        };
    }
    return {
        ok: true,
    };
};
module.exports.usersRequired = 21;
module.exports.timeRequired = 10;
module.exports.description = "Correct bar chart";

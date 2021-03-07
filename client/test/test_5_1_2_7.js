const fetch = require("node-fetch");

const {
    SERVER_HOST,
    SERVER_HOST_WS,
    sleep,
    sleepMs,
    getWebSocketPromise,
} = require("./common");

/**
 * Checks that trending words are correctly calculated with the following restrictions:
 * 1. the words "good", "interesting", "fun" written 10 times. The test will succeed if these are the top 3 words.
 * 2. the words "as", "a", "and" (these will be taken from a list of common words to ignore) written 20 times. The test will fail if any of these are in the top 3
 * 3. the words "bad, "boring", "tiring" written 15 time. The test will succeed if these are the new top 3 words.
 * @param {Array} users
 */
module.exports = async function test_5_1_2_7(users) {
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

    async function tryWords(testWords, numberOfUsers, shouldBeTrends = true) {
        let body = JSON.stringify({
            anonymous: false,
            responses: [{ questionID: "0", response: testWords.join(" ") }],
        });
        let userSplice = users.splice(0, numberOfUsers);
        for (let user of userSplice) {
            user.request = fetch(
                `${SERVER_HOST}/api/event/${eventCode}/feedback`,
                {
                    method: "POST",
                    body,
                    headers: {
                        cookie: user.cookie,
                    },
                }
            );
            await sleepMs(20); // Wait between requests, since the server struggles a little
        }
        let failure = null;
        for (let user of userSplice) {
            let json = await (await user.request).json();
            if (json.status != "success") {
                failure = {
                    ok: false,
                    message: "Could not properly give feedback; request failed",
                };
            }
        }
        if (failure) return failure;
        await sleep(3); // Wait 3 seconds for new data
        if (wsData == null) {
            return {
                ok: false,
                message: "WebSocket data was not supplied within 3 seconds",
            };
        }
        let trends = [];
        for (let trend of wsData.questions[0].trends) {
            trends.push(trend.phrase.toLowerCase());
        }
        for (let testWord of testWords) {
            if (trends.includes(testWord) !== shouldBeTrends) {
                failure = {
                    ok: false,
                    message:
                        "Word '" +
                        testWord +
                        (shouldBeTrends
                            ? "' was not in the trends when it should have been"
                            : "' was in the trends when it should not have been"),
                };
            }
        }
        if (failure) return failure;
        return {
            ok: true,
        };
    }

    let res = await tryWords(["good", "interesting", "fun"], 10, true);
    if (!res.ok) {
        await ws.close();
        return res;
    }
    res = await tryWords(["as", "a", "and"], 20, false);
    if (!res.ok) {
        await ws.close();
        return res;
    }
    res = await tryWords(["bad", "boring", "tiring"], 15, true);
    if (!res.ok) {
        await ws.close();
        return res;
    }
    // use token to establish web socket connection
    await ws.close();
    // await message from ws
    return {
        ok: true,
    };
    //
};

module.exports.usersRequired = 46;
module.exports.timeRequired = 10;
module.exports.description = "Correct trending words";

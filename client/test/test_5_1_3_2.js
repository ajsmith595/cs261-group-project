const fetch = require("node-fetch");
const { SERVER_HOST } = require("./common");
/**
 * Check feedback is rejected if invalid
 * @param {Array} users
 */
module.exports = async function test_5_1_3_2(users) {
    let host = users.splice(0, 1)[0];
    let startTime = new Date().getTime() + 1000 * 2; // Time + 2 seconds
    let eventRequest = await fetch(`${SERVER_HOST}/api/events`, {
        method: "POST",
        headers: {
            cookie: host.cookie,
        },
        body: JSON.stringify({
            title: "Test Event",
            startTime,
            duration: 60,
            questions: [
                {
                    type: "numeric",
                    title: "Test Question 1",
                    min: 0,
                    max: 10,
                },
            ],
        }),
    });
    if (!eventRequest.ok) {
        return {
            ok: false,
            message: "Could not create new event",
        };
    }
    let response = await eventRequest.json();
    if (response.status != "success") {
        return {
            ok: false,
            message: "Could not create new event (no success status)!",
        };
    }
    let { eventCode } = response.data;

    let invalidResponses = [
        {
            anonymous: false,
        },
        {
            anonymous: false,
            responses: [],
        },
        {
            anonymous: false,
            responses: [{ questionID: "0" }],
        },
        {
            anonymous: false,
            responses: [{ response: "Hello World" }],
        },
    ];
    let i = 0;
    for (let invalidResponse of invalidResponses) {
        i++;
        let user = users.pop();
        let response = await (
            await fetch(`${SERVER_HOST}/api/event/${eventCode}/feedback`, {
                method: "POST",
                body: JSON.stringify(invalidResponse),
                headers: {
                    cookie: user.cookie,
                },
            })
        ).json();

        if (response.status === "success") {
            return {
                ok: false,
                message: `Invalid response (${i}) (${eventCode}) was accepted!`,
            };
        }
    }

    let user = users.pop();
    response = await (
        await fetch(`${SERVER_HOST}/api/event/${eventCode}/feedback`, {
            method: "POST",
            body: JSON.stringify({
                anonymous: false,
                responses: [
                    {
                        questionID: "0",
                        response: "Hello World!",
                    },
                ],
            }),
            headers: {
                cookie: user.cookie,
            },
        })
    ).json();

    if (response.status !== "success") {
        return {
            ok: false,
            message: "Valid feedback was rejected!",
        };
    }
    return {
        ok: true,
    };
};
module.exports.usersRequired = 6;
module.exports.timeRequired = 10;
module.exports.description = "Invalid feedback rejection";

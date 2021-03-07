const fetch = require("node-fetch");
const { SERVER_HOST, sleep } = require("./common");

/**
 * Checks to see that the 1 response / minute restriction is followed
 * @param {Array} users
 */
module.exports = async function test_5_1_1_1(users) {
    if (users.length != 2)
        throw new Error("Wrong number of users passed to function");
    let host = users[0];
    let attendee = users[1];

    let timeNow = new Date().getTime() + 1000 * 2; // Time + 2 seconds
    let eventRequest = await fetch(`${SERVER_HOST}/api/events`, {
        method: "POST",
        headers: {
            cookie: host.cookie,
        },
        body: JSON.stringify({
            title: "Test Event",
            startTime: timeNow,
            duration: 60,
            questions: [
                {
                    type: "open",
                    title: "Test Question 1",
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

    await sleep(2); // Wait 2 seconds so feedback is valid
    let eventCode = response.data.eventCode;

    let feedbackBody = {
        method: "POST",
        headers: {
            cookie: attendee.cookie,
        },
        body: JSON.stringify({
            anonymous: false,
            responses: [
                {
                    questionID: "0",
                    response: "Test Response",
                },
            ],
        }),
    };
    let feedbackRequestPrimary = await (
        await fetch(
            `${SERVER_HOST}/api/event/${eventCode}/feedback`,
            feedbackBody
        )
    ).json();

    if (feedbackRequestPrimary.status != "success") {
        return {
            ok: false,
            message: "Primary feedback request failed",
        };
    }

    let feedbackRequest0 = await (
        await fetch(
            `${SERVER_HOST}/api/event/${eventCode}/feedback`,
            feedbackBody
        )
    ).json();
    if (feedbackRequest0.status == "success") {
        return {
            ok: false,
            message: "Immediate feedback request succeeded; should have failed",
        };
    }
    await sleep(30);
    let feedbackRequest30 = await (
        await fetch(
            `${SERVER_HOST}/api/event/${eventCode}/feedback`,
            feedbackBody
        )
    ).json();
    if (feedbackRequest30.status == "success") {
        return {
            ok: false,
            message: "30 second feedback request succeeded; should have failed",
        };
    }
    await sleep(59 - 30);
    let feedbackRequest59 = await (
        await fetch(
            `${SERVER_HOST}/api/event/${eventCode}/feedback`,
            feedbackBody
        )
    ).json();
    if (feedbackRequest59.status == "success") {
        return {
            ok: false,
            message: "59 second feedback request succeeded; should have failed",
        };
    }
    await sleep(61 - 59);
    let feedbackRequest61 = await (
        await fetch(
            `${SERVER_HOST}/api/event/${eventCode}/feedback`,
            feedbackBody
        )
    ).json();
    if (feedbackRequest61.status != "success") {
        return {
            ok: false,
            message: "61 second feedback request failed; should have succeeded",
        };
    }
    await sleep(90);
    let feedbackRequest90 = await (
        await fetch(
            `${SERVER_HOST}/api/event/${eventCode}/feedback`,
            feedbackBody
        )
    ).json();
    if (feedbackRequest90.status != "success") {
        return {
            ok: false,
            message: "90 second feedback request failed; should have succeeded",
        };
    }
    return {
        ok: true,
    };
};

module.exports.usersRequired = 2;
module.exports.timeRequired = 151;
module.exports.description = "Feedback timeout";
module.exports.after = true;

const fetch = require("node-fetch");
const { SERVER_HOST, sleep } = require("./common");
/**
 * Checks the event creation is correctly validated with the following restrictions:
 * 1. the title is empty. Success on a refused request.
 * 2. the duration is 5 seconds. Success on a refused request.
 * 3. the duration is 4 minutes 59 seconds. Success on a refused request.
 * 4. the start time is in the past. Success on a refused request.
 * 5. all fields are valid. Success on accepted request.
 * @param {Array} users
 */
module.exports = async function test_5_1_2_1(users) {
    let host = users[0];
    let timeNow = new Date().getTime();
    let timeTomorrow = new Date();
    timeTomorrow.setDate(timeTomorrow.getDate() + 1);
    timeTomorrow = timeTomorrow.getTime();

    let timeYesterday = new Date();
    timeYesterday.setDate(timeYesterday.getDate() - 1);
    timeYesterday = timeYesterday.getTime();
    async function test(title, startTime, duration, accept = false) {
        let eventRequest = await (
            await fetch(`${SERVER_HOST}/api/events`, {
                method: "POST",
                headers: {
                    cookie: host.cookie,
                },
                body: JSON.stringify({
                    title: title,
                    startTime: startTime,
                    duration: duration,
                    questions: [
                        {
                            type: "open",
                            title: "Test Question 1",
                        },
                    ],
                }),
            })
        ).json();
        if ((eventRequest.status == "success") != accept) return false;
        return true;
    }

    if (!(await test("", timeTomorrow, 60))) {
        return {
            ok: false,
            message: "Empty title was accepted; should have been rejected",
        };
    }
    if (!(await test("Test Event", timeTomorrow, 5 / 60))) {
        return {
            ok: false,
            message:
                "Event with 5 seconds duration was accepted; should have been rejected",
        };
    }
    if (!(await test("Test Event", timeTomorrow, 4 + 59 / 60))) {
        return {
            ok: false,
            message:
                "Event with 4 minutes 59 seconds duration was accepted; should have been rejected",
        };
    }
    if (!(await test("Test Event", timeYesterday, 60))) {
        return {
            ok: false,
            message:
                "Event with a start time in the past was accepted; should have been rejected",
        };
    }
    if (!(await test("Test Event", timeTomorrow, 60, true))) {
        return {
            ok: false,
            message: "Valid event was rejected; should have been accepted",
        };
    }
    return { ok: true };
};

module.exports.usersRequired = 1;
module.exports.timeRequired = 0;
module.exports.description = "Event validation";

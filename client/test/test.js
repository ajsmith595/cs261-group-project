const SERVER_HOST = require("./common").SERVER_HOST;
const fetch = require("node-fetch");

const test_5_1_1_1 = require("./test_5_1_1_1");
const test_5_1_1_4 = require("./test_5_1_1_4");
const test_5_1_2_1 = require("./test_5_1_2_1");
const test_5_1_2_7 = require("./test_5_1_2_7");
const test_5_1_2_8 = require("./test_5_1_2_8");
const test_5_1_2_9 = require("./test_5_1_2_9");
const test_5_1_2_10 = require("./test_5_1_2_10");
const test_6_2_0_1 = require("./test_6_2_0_1");
const test_6_2_0_3 = require("./test_6_2_0_3");

runTests();
// 5.1.1.1
// feedback only every minute at most

async function runTests() {
    let timestamp = Math.floor(new Date().getTime() / 1000);

    function getEmail(num) {
        return `test_${timestamp}_${num}@test.com`;
    }
    function getUsername(num) {
        return `t${timestamp}${num}`;
    }

    let tests = [
        test_5_1_1_1,
        test_5_1_1_4,
        test_5_1_2_1,
        test_5_1_2_7,
        test_5_1_2_8,
        test_5_1_2_9,
        test_5_1_2_10,
        test_6_2_0_1,
        test_6_2_0_3,
    ];

    let numberOfUsersToCreate = 0;
    for (let x of tests) {
        numberOfUsersToCreate += x.usersRequired;
    }

    let requests = [];
    for (let i = 0; i < numberOfUsersToCreate; i++) {
        let username = getUsername(i);
        let email = getEmail(i);
        let req = fetch(`${SERVER_HOST}/api/register`, {
            method: "POST",
            body: JSON.stringify({
                username,
                email,
            }),
        });
        requests.push({
            request: req,
            username,
            email,
        });
    }

    for (let i in requests) {
        let response = await requests[i].request;
        requests[i].cookie = response.headers.get("set-cookie");
    }

    // Segment them out.
    tests.sort((a, b) => a.timeRequired - b.timeRequired);

    let maxTextLength = 0;
    for (let test of tests) {
        test.text = `${test.description} (${test.name}):`;
        if (test.text.length > maxTextLength) maxTextLength = test.text.length;
        test.promise = test(requests.splice(0, test.usersRequired));
    }
    try {
        for (let test of tests) {
            let text = test.text.padEnd(maxTextLength, " ");
            process.stdout.write(
                `${text} \u001b[37m\u001b[40mWAITING\x1b[0m\r`
            );
            test.result = await test.promise;
            if (!test.result || !test.result.ok) {
                if (!test.result) {
                    console.log(`${text} \x1b[30m\x1b[43mNO RESULT\x1b[0m`);
                } else {
                    console.log(
                        `${text} \x1b[41m\x1b[37mFAIL: ${test.result.message}\x1b[0m`
                    );
                }
            } else {
                console.log(`${text} \x1b[42m\x1b[30mSUCCESS\x1b[0m`);
            }
        }
    } catch (e) {
        console.log(e);
        console.log(
            "An error occured whilst running the tests. This usually happens if the server has just started. Rerun the script for actual results."
        );
    }
}

// 5.1.2.10
// INACCURATE

// 5.1.2.11
// Choice question, not numeric

// 5.1.3.2
// Missing data for feedback

// 6.2.0.1
// Create 100 users and connect to one event. Test fails if feedback takes more than 3 seconds to arrive in host view.

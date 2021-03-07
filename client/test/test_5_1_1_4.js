const fetch = require("node-fetch");
const { SERVER_HOST, sleep } = require("./common");

/**
 * Checks to see that the usernames and emails are properly validated with the following tests:
 * 1. only 3 alphanumeric characters. Success on a refused request.
 * 2. only 3 characters, with some alphanumeric ones. Success on a refused request.
 * 3. 17 alphanumeric characters. Success on a refused request.
 * 4. 17 characters, with some alphanumeric ones. Success on a refused request.
 * 5. between 4 and 16 characters, with at least one non-alphanumeric character. Success on a refused request.
 * 6. between 4 and 16 alphanumeric characters. Success on accepted request.
 * 7. the same as the the username from the test above (i.e an account that was successfully created).
 * @param {Array} users
 */
module.exports = async function test_5_1_1_4(users) {
    const timestamp = new Date().getTime();
    async function test(username, email, accept = false) {
        let response = await (
            await fetch(`${SERVER_HOST}/api/register`, {
                method: "POST",
                body: JSON.stringify({
                    username,
                    email,
                }),
            })
        ).json();
        if ((response.status === "success") != accept) {
            return false;
        }
        return true;
    }
    if (!(await test("aaa", `test_${timestamp}@test.com`))) {
        return {
            ok: false,
            message:
                "3 character username validation succeeded: should have failed",
        };
    }
    if (!(await test("a'1", `test_${timestamp}@test.com`))) {
        return {
            ok: false,
            message:
                "3 character non-alphanumeric username validation succeeded: should have failed",
        };
    }
    if (!(await test("aaa111222333bbb11", `test_${timestamp}@test.com`))) {
        return {
            ok: false,
            message:
                "17 character username validation succeeded: should have failed",
        };
    }
    if (!(await test("aaa1'_&22333bbb11", `test_${timestamp}@test.com`))) {
        return {
            ok: false,
            message:
                "17 character non-alphanumeric username validation succeeded: should have failed",
        };
    }
    if (!(await test("test'_123", `test_${timestamp}@test.com`))) {
        return {
            ok: false,
            message:
                "Valid length non-alphanumeric username validation succeeded: should have failed",
        };
    }
    if (!(await test(`t${timestamp}`, `test_${timestamp}@test.com`, true))) {
        return {
            ok: false,
            message: "Valid username validation failed: should have succeeded",
        };
    }
    if (!(await test("test123", `test_${timestamp}_duplicate@test.com`))) {
        return {
            ok: false,
            message:
                "Valid duplicate username validation succeeded: should have failed",
        };
    }
    return {
        ok: true,
    };
};

module.exports.usersRequired = 0;
module.exports.timeRequired = 0;
module.exports.description = "Username validation";

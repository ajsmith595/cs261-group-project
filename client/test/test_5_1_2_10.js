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
module.exports = async function test_5_1_2_10(users) {};
module.exports.usersRequired = 6;
module.exports.timeRequired = 60;
module.exports.description = "Correct rolling average";

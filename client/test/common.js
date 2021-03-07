const fetch = require("node-fetch");
const WebSocket = require("ws");
const baseURL = "localhost:4567";
const SERVER_HOST = `http://${baseURL}`;
const SERVER_HOST_WS = `ws://${baseURL}`;
const sleepMs = function (ms) {
    return new Promise((x) => setTimeout(x, ms));
};
const sleep = function (s) {
    return module.exports.sleepMs(s * 1000);
};
module.exports.SERVER_HOST = SERVER_HOST;
module.exports.SERVER_HOST_WS;
module.exports.sleepMs = sleepMs;
module.exports.sleep = sleep;

module.exports.getWebSocketPromise = async function (host, questions) {
    let startTime = new Date().getTime() + 2 * 1000; // Start in 2 seconds
    let eventRequest = await (
        await fetch(`${SERVER_HOST}/api/events`, {
            method: "POST",
            headers: {
                cookie: host.cookie,
            },
            body: JSON.stringify({
                title: "Test Event",
                startTime: startTime,
                duration: 60,
                questions,
            }),
        })
    ).json();
    if (eventRequest.status !== "success") {
        return {
            ok: false,
            message: "Event creation failed",
        };
    }

    await sleep(2); // Wait 2 seconds
    let eventCode = eventRequest.data.eventCode;

    let response = await (
        await fetch(`${SERVER_HOST}/api/event/${eventCode}/token`, {
            headers: {
                cookie: host.cookie,
            },
        })
    ).json();

    if (response.status !== "success") {
        return {
            ok: false,
            message: "Getting WS token failed",
        };
    }
    let token = response.data;

    let promise = new Promise(function (resolve, reject) {
        let ws = new WebSocket(SERVER_HOST_WS + "/socket", {
            headers: {
                Cookie: host.cookie,
            },
        });
        ws.on("open", (x) => {
            resolve(ws);
        });
        ws.on("error", (e) => {
            reject(e);
        });
    });
    return {
        ok: true,
        data: {
            promise,
            token,
            eventCode,
        },
    };
};

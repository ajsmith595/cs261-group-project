/* eslint-disable no-restricted-globals */

const ignored = self.__WB_MANIFEST;

self.addEventListener("fetch", function (event) {
    if (!navigator.onLine) {
        event.respondWith(
            new Response(
                "<html><h1>Sorry, it seems you're offline.</h1></html>",
                {
                    headers: { "Content-Type": "text/html" },
                }
            )
        );
    }
});

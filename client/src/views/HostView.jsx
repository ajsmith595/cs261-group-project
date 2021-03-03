import React from "react";
import { Button, Row, Col } from 'react-bootstrap';
import { Bar, Line } from 'react-chartjs-2';
import interpolate from 'color-interpolate';
import { Spring, Transition } from 'react-spring/renderprops';
import 'chartjs-plugin-annotation';
export default class HostView extends React.Component {
    constructor(props) {
        super(props);


        // Bind all the functions so that the value of `this` is correct
        this.webSocketOpen = this.webSocketOpen.bind(this);
        this.webSocketDataReceived = this.webSocketDataReceived.bind(this);
        this.webSocketError = this.webSocketError.bind(this);
        this.webSocketClose = this.webSocketClose.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
        this.reconnect = this.reconnect.bind(this);

        // At first, it'll be loading
        this.state = {
            status: 'loading'
        };
        this.socket = null;
    }

    componentDidMount() {
        document.title = "Hosting '" + this.props.data.title + "'";
        if (process.env.NODE_ENV === 'development' && !process.env.REACT_APP_USE_WEBSOCKETS) {
            let sample = require("../sample_data/HostView.json");
            this.setState(sample);
        }
        else {
            this.socket = new WebSocket(process.env.REACT_APP_WS_ADDRESS || ("ws://" + window.location.host + "/socket"));
            // Listen for messages
            this.socket.addEventListener('open', this.webSocketOpen);
            this.socket.addEventListener('message', this.webSocketDataReceived);
            this.socket.addEventListener('error', this.webSocketError);
            this.socket.addEventListener('close', this.webSocketClose);
        }
    }
    webSocketOpen(e) {
        // When the WebSocket first connects, the server expects the client to 'authenticate' with a token. We need to get the token, and then send it to the websocket.
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + `/api/event/${this.props.eventID}/token`, {
            credentials: "include"
        }).then(e => e.json()).then(data => {
            this.socket.send(data.token); // authenticate with the token
        }).catch(e => {
            // If something failed, we should close the connection - the client can always reload the page/click the reconnect button to reconnect
            this.setState({
                status: 'closed'
            });
            this.socket.close();
        });
    }
    webSocketClose(e) {
        this.setState({
            status: 'closed'
        });
        this.socket.close(); // Make sure it's closed
    }
    webSocketError(e) {
        // If the WebSocket errors, show the error to the user.
        this.setState({
            status: 'error',
            error: e
        });
        this.socket.close();
    }
    webSocketDataReceived(event) {
        // When we receive new data, we need to update the display.
        // The following will take the questions, and assign 'previous' values to the mood (smiley face animation), and trend values (trend word font sizes).
        // TODO: the trends do not take into account the fact that the actual trend word/phrase could change, so not a nice animation for that.
        let data = JSON.parse(event.data);
        let questions = data.questions;
        for (let i in questions) { // Go through the questions, and assign their 'previous' values - this allows for animation.
            let question = questions[i];
            if (question.type === 'open') {
                question.previous_mood = 0;
                if (this.state.feedback) {
                    question.previous_mood = this.state.feedback[i].current_mood;
                }

                for (let j in question.trends) {
                    let trend = question.trends[j];
                    trend.previous_proportion = 30;
                    if (this.state.feedback && this.state.feedback[i].trends[j]) {
                        trend.previous_mood = this.state.feedback[i].trends[j].proportion;
                    }

                }
            }
        }
        let stats = {
            mins_left: data.mins_left,
            total_responses: data.total_responses,
            total_users: data.total_users
        };
        this.setState({
            feedback: questions,
            status: 'show',
            stats
        });
    }
    // If the component is gonna be removed, close the websocket connection.
    componentWillUnmount() {
        if (this.socket) {
            this.socket.close();
            //TODO: unbind all the event handlers
        }
    }


    // The view for a particular NUMERIC question e.g. rate the event from 1->10
    numericView(question) {
        let actualData = [];
        for (let point of question.points) {
            actualData.push({
                t: new Date(point.time * 1000), // NOTE: expects the "time" to be a timestamp in SECONDS
                y: point.value
            });
        }
        actualData.push({
            t: new Date(question.stats.current_time * 1000),
            y: question.stats.current_value
        });

        //#region Chart JS Configuration
        let data = {
            labels: [question.title],
            datasets: [{
                label: question.title,
                data: actualData,
                borderColor: ["rgb(26, 80, 139)"],
                backgroundColor: ["rgba(26, 80, 139, 0.3)"]
            }]
        };

        let options = {
            legend: {
                display: false
            },
            scales: {
                yAxes: [{
                    ticks: {
                        suggestedMin: question.min_value,
                        suggestedMax: question.max_value
                    }
                }],
                xAxes: [
                    {
                        ticks: {
                            min: new Date(question.min_time * 1000),
                            max: new Date(question.max_time * 1000)
                        },
                        type: 'time',
                        time: {
                            unit: 'minute',
                            stepSize: 15
                        }
                    }
                ]
            },
            annotation: {
                annotations: [
                    {
                        type: "line",
                        mode: "vertical",
                        scaleID: "x-axis-0",
                        value: new Date(question.current_time * 1000),
                        borderColor: "black",
                        borderWidth: 1,
                        borderDash: [4, 4],
                        label: {
                            backgroundColor: "#000",
                            content: "NOW",
                            enabled: true
                        }
                    }
                ]
            }
        };
        //#endregion Chart JS Configuration

        // TODO: change the 'TIME LEFT' to something else - we should have event-level stats like "TIME LEFT" and "NO ATTENDEES" at the top of the page, at some point
        return (
            <Col className="p-3 border border-secondary" sm={12} lg={6}>
                <Row>
                    <Col className="d-xs-none" sm={1}></Col>
                    <Col xs={12} sm={2} className="">
                        <Row>
                            <Col className="border py-2 px-1">
                                <h4 className="mb-0">{question.stats.max_value}</h4>
                                <small>BEST</small>
                            </Col>
                            <Col className="border py-2 px-1">
                                <h4 className="mb-0">{question.stats.min_value}</h4>
                                <small>WORST</small>
                            </Col>
                        </Row>
                    </Col>
                    <Col xs={12} sm={6}>
                        <h3>{question.title}</h3>
                    </Col>
                    <Col xs={12} sm={2}>
                        <Row>
                            <Col className="border py-2 px-1">
                                <h4 className="mb-0">{question.stats.overall_average?.toFixed(2)}</h4>
                                <small>OVERALL</small>
                            </Col>
                            <Col className="border py-2 px-1">
                                <h4 className="mb-0">{question.stats.current_value.toFixed(2)}</h4>
                                <small>CURRENT</small>
                            </Col>
                        </Row>
                    </Col>
                    <Col className="d-xs-none" sm={1}></Col>
                </Row>
                <Line options={options} data={data} width={100} height={30}></Line>
            </Col>
        );
    }

    // The view for one particular OPEN question (open textbox e.g. general feedback)
    openView(question) {
        let recent_responses = (
            <Transition
                items={question.recent_responses}
                keys={item => item.id}
                from={{ opacity: 0 }}
                enter={{ opacity: 1 }}
                leave={{ opacity: 0 }}
                config={{ friction: 100 }}
            // Fade in each response like a chat message. Height animation is pretty annoying so sticking with just opacity with this one. Can always do the annoying height animation later
            >
                {response => props =>
                (
                    <p key={response.id} style={props}><span className="font-weight-bold">{response.username || "Anonymous"}</span>: {response.message}</p>
                )
                }
            </Transition>
        );
        let trends = [];
        for (let trend of question.trends) { // Keep the height constant, and just change the scaling of the text. Easier to manage and better performance
            trends.push(
                <div className="w-100" style={{ height: "33%" }}>
                    <Spring from={{ proportion: trend.previous_proportion }} to={{ proportion: trend.proportion }}>
                        {props => ( // Animates the proportion so that the text will animate large <--> small                        
                            <p style={{ transform: "scale(" + Math.min(Math.max(props.proportion / 100 * 4, 0.5), 2) + ")" }}>{trend.phrase}</p>
                        )}
                    </Spring>
                </div>
            );
        }

        return (<Spring from={{ current_mood: question.previous_mood }} to={{ current_mood: question.current_mood }}>
            {props => { // Transition the face!
                let mood = props.current_mood
                let y = 65 - mood * 5; // adjusts the line depending on the mood. Makes it so that the mouth looks somewhat in a normal position
                let sweep_flag = mood > 0 ? 0 : 1; // Makes the curve go up or down -> happy or sad
                let radius = Math.abs(24 / (Math.abs(mood) < 0.01 ? 0.01 : mood)); // if the mood is 0, make it realllyyy small instead - prevent division by 0.
                let colour_interpolator = interpolate(["#D2222D", "#FFBF00", "#389338"]);
                let colour = colour_interpolator((mood + 1) / 2); // interpolate between red -> yellow -> green
                return (
                    <Col className="p-2 border border-secondary d-flex flex-column" sm={12} lg={6}>
                        <h3>{question.title}</h3>
                        <Row className="flex-grow-1">
                            <Col xs={12} lg={4}>
                                <div className="h-100 p-1 border d-flex flex-column">
                                    <h4>Recent Responses</h4>
                                    <hr className="w-100" />
                                    <div className="text-left flex-grow-1 position-relative overflow-hidden" style={{ minHeight: "100px" }} >
                                        <div className="position-absolute">
                                            {recent_responses}
                                        </div>
                                    </div>
                                </div>
                            </Col>
                            <Col xs={12} lg={4}>
                                <div className="h-100 p-1 border mt-1 mt-sm-0 d-flex flex-column">
                                    <h4>Trends</h4>
                                    <hr className="w-100" />
                                    <Row className="flex-grow-1" style={{ overflowX: 'hidden' }}>
                                        {trends}
                                    </Row>
                                </div>
                            </Col>
                            <Col xs={12} lg={4}>
                                <div className="h-100 p-1 border mt-2 mt-sm-0 d-flex flex-column">
                                    <h4>Current Mood</h4>
                                    <hr className="w-100" />
                                    <div className="flex-grow-1">
                                        <svg width="50%" viewBox="0 0 100 100">

                                            <circle cx="50" cy="50" r="40" stroke="black" strokeWidth="4" fill={colour} />

                                            <circle cx="40" cy="40" r="4" fill="black" />
                                            <circle cx="60" cy="40" r="4" fill="black" />

                                            <path d={`M 30 ${y} A ${radius} ${radius} 0 0 ${sweep_flag} 70 ${y}`} stroke="black"
                                                strokeWidth="5" fill="none" />

                                        </svg>
                                    </div>
                                </div>
                            </Col>
                        </Row>
                    </Col>
                );
            }}
        </Spring>);


    }


    // The view for a particular CHOICE question (e.g. what's your favourite colour? Red/Green/Blue)
    choiceView(question) {


        // Note: pretty much all of this is just chart.js configuration
        let names = [];
        let values = [];
        let colours = [];
        let allColours = ["red", "blue", "green", "yellow"]; // Not related to data. These are the colours used by Chart JS for each of the options. 
        let currentColourIndex = 0;
        for (let obj of question.options) {
            names.push(obj.name);
            values.push(obj.number);
            colours.push(allColours[currentColourIndex]);
            currentColourIndex = (currentColourIndex + 1) % allColours.length; // For now, just use 4 difference colours for the options. If there's more than 4 data points, it will just go red -> blue -> green -> yellow -> red -> blue -> ...
        }


        let data = {
            labels: names,
            datasets: [{
                display: false,
                data: values,
                backgroundColor: colours,
            }]
        };
        let options = {
            legend: {
                display: false
            },
            scales: {
                yAxes: [{
                    ticks: {
                        suggestedMin: 0,
                        precision: 0
                    }
                }]
            }
        };

        return (
            <Col className="p-3 border border-secondary" sm={12} lg={6}>
                <h3>{question.title}</h3>
                <Bar data={data} options={options} width={100} height={30}></Bar>
            </Col>
        );
    }

    render() {
        if (this.state.status === 'show') {
            let feedback = this.state.feedback;
            let divs = [];
            for (let question of feedback) {
                switch (question.type) {
                    case 'numeric':
                        divs.push(this.numericView(question));
                        break;
                    case 'choice':
                        divs.push(this.choiceView(question));
                        break;
                    case 'open':
                        divs.push(this.openView(question));
                        break;
                    default:
                        break;
                }
            }
            // TODO: change this 'props.event' so it uses the new input data.
            let time_left_display;
            if (this.props.data.duration < this.state.stats.mins_left || !this.state.stats) {
                time_left_display = (
                    <Col xs={12} lg={2} className="border">
                        <h1 className="font-weight-bold lead h-100 d-flex justify-content-center align-items-center">THIS EVENT HAS NOT STARTED</h1>
                    </Col>
                );
            } else if (this.state.stats.mins_left <= 0) {
                time_left_display = (
                    <Col xs={12} lg={2} className="border">
                        <h1 className="font-weight-bold lead h-100 d-flex justify-content-center align-items-center">THIS EVENT HAS ENDED</h1>
                    </Col>
                );
            } else {
                let time;
                if (this.state.stats?.mins_left > 60) {
                    time = <h2 className="mb-0">{Math.floor(this.state.stats?.mins_left / 60)}h {this.state.stats?.mins_left % 60}m</h2>;
                }
                else {
                    time = <h2 className="mb-0">{this.state.stats?.mins_left}<small>mins</small></h2>;
                }

                time_left_display = (
                    <Col xs={12} lg={2} className="border">
                        {time}
                        <small>TIME LEFT</small>
                    </Col>
                );
            }
            return (
                <div className="text-center p-3">
                    <Row className="mb-2">
                        <Col xs={12} lg={2} className="border">
                            <h2 className="mb-0">{this.props.eventID}</h2>
                            <small>EVENT CODE</small>
                        </Col>
                        {time_left_display}
                        <Col lg={4}>
                            <h1>{this.props.data.title || "Unknown Event"}</h1>
                        </Col>
                        <Col lg={2} className="border">
                            <h2 className="mb-0">{this.state.stats?.total_users}</h2>
                            <small>ATTENDEES</small>
                        </Col>
                        <Col lg={2} className="border">
                            <h2 className="mb-0">{this.state.stats?.total_responses}</h2>
                            <small>RESPONSES</small>
                        </Col>
                    </Row>
                    <Row>
                        {divs}
                    </Row>
                </div>
            );
        }
        else if (this.state.status === 'loading') {
            return (
                <h1 className="text-center">Loading...</h1>
            )
        } else if (this.state.status === 'error') { // TODO: change so that you get a more detailed error
            return (
                <h1 className="text-center">There was a problem with the WebSocket connection :/</h1>
            )
        }
        else if (this.state.status === 'closed') {
            return (
                <div className="text-center">
                    <h1>The server closed the connection.</h1>
                    <Button type="primary" onClick={this.reconnect}>Reconnect</Button>
                </div>
            )
        }
        else {
            return null;
        }
    }

    reconnect() {
        this.setState({
            status: 'loading'
        });
        this.componentDidMount(); // Simulate the component being readded to cause the websocket reconnection
    }
}
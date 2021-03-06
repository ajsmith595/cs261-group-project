import React from "react";
import FeedbackView from "./FeedbackView";
import HostView from "./HostView";
import { Form, Col, Row, Button, InputGroup } from "react-bootstrap";

export default class EditEventView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            status: 'loading',
            validationErrors: []
        };
    }

    changeEventProp(prop, e) {
        let newValidationErrors = this.state.validationErrors.slice(0).filter(e => e != prop && e != prop + "_min" && e != prop + "_max");

        if (prop == "title") {
            if (e.target.value.length == 0) {
                newValidationErrors.push("title");
            }
        }
        else if (prop == "duration") {
            let value = parseInt(e.target.value);
            if (isNaN(value) || value < 5) {
                newValidationErrors.push("duration_min");
            }
            else if (value > 12 * 60) {
                newValidationErrors.push("duration_max");
            }
        }
        this.setState({
            [prop]: e.target.value,
            validationErrors: newValidationErrors
        });
    }
    componentDidMount() {
        document.title = "Edit event";
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + `/api/event/${this.props.match.params.id}`, {
            credentials: "include"
        }).then(res => res.json()).then(e => {
            if (e.status == 'success' && e.data.isHost) {
                let date = (new Date(e.data.startTime)).toISOString();
                date = date.substring(0, date.length - 1);
                document.title = "Edit '" + e.data.title + "'";
                this.setState({
                    status: 'main',
                    title: e.data.title,
                    startTime: date,
                    duration: e.data.duration
                });
            }
            else {
                this.setState({
                    status: 'error'
                })
            }
            console.log(e);
        }).catch(e => {
            this.setState({
                status: 'error'
            });
        });
    }

    submit() {
        let startTime = this.state.startTime;
        if (!(startTime instanceof Date)) {
            startTime = new Date(startTime);
        }
        startTime = startTime.getTime();
        let data = {
            title: this.state.title,
            startTime,
            duration: this.state.duration
        };
        this.setState({
            status: 'loading'
        })
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + `/api/event/${this.props.match.params.id}/edit`, {
            credentials: "include",
            method: "POST",
            body: JSON.stringify(data)
        }).then(res => res.json()).then(e => {
            if (e.status == "success") {
                this.props.history.push(`/event/${this.props.match.params.id}`);
            }
            else {
                this.setState({
                    status: 'error'
                })
            }
        }).catch(e => {
            this.setState({
                status: 'error'
            });
        });
    }
    render() {
        if (this.state.status === "loading") {
            return <h1 className="text-center">Loading...</h1>
        } else if (this.state.status === "error") {
            return <h1 className="text-center">An error occurred!</h1>
        }
        else {
            return (
                <div className="text-left align-middle py-2">
                    <Row>
                        <Col />
                        <Col xs={12} sm={12} md={10} lg={6}>
                            <h1 className="text-center">Edit event</h1>
                            {/* Title */}
                            <Form.Group>
                                <Form.Label className="w-100">Title <span className="text-danger float-right">{this.state.validationErrors.includes("title") ? "The title cannot be empty" : ""}</span></Form.Label>
                                <Form.Control className={this.state.validationErrors.includes("title") ? 'border-danger' : ''} type="text" name="title" value={this.state.title} placeholder="Event Title" onChange={(e) => this.changeEventProp("title", e)} />
                            </Form.Group>

                            {/* Date and Time */}
                            <Form.Row>
                                <Form.Group as={Col} xs={12} sm={6} lg={7}>
                                    <Form.Label>Start Date/Time</Form.Label>
                                    <Form.Control type="datetime-local" value={this.state.startTime} onChange={(e) => this.changeEventProp("startTime", e)} />
                                </Form.Group>
                                <Form.Group as={Col} xs={12} sm={6} lg={5}>
                                    <Form.Label className="w-100">Duration<span className="float-right text-danger">{this.state.validationErrors.includes("duration_min") ? 'Must be at least 5 mins' : (this.state.validationErrors.includes("duration_max") ? 'Must be at most 12 hours' : '')}</span></Form.Label>
                                    <InputGroup>
                                        <Form.Control type="number" min="5" max={60 * 12} className={(this.state.validationErrors.includes("duration_min") || this.state.validationErrors.includes("duration_max")) ? 'border-danger' : ''} value={this.state.duration} onChange={(e) => this.changeEventProp("duration", e)} />
                                        <InputGroup.Append>
                                            <InputGroup.Text id="startDatePrepend">
                                                <span className="d-none d-md-block">minutes</span>
                                                <span className="d-block d-md-none">mins</span>
                                            </InputGroup.Text>
                                        </InputGroup.Append>
                                    </InputGroup>
                                </Form.Group>
                            </Form.Row>
                            <div className="text-center">
                                <Button variant="primary" disabled={this.state.validationErrors.length > 0} onClick={() => this.submit()}>Update Event</Button>
                            </div>
                        </Col>
                        <Col />
                    </Row>
                </div>
            );
        }
    }
}
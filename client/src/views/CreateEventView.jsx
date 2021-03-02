import React from "react";
import { Redirect } from 'react-router-dom';
import { Form, Col, Row, Button, InputGroup } from "react-bootstrap";

export default class CreateEventView extends React.Component {
    constructor(props) {
        super(props);


        this.submit = this.submit.bind(this);
        // Default date/time is the next hour available (so 12:36 -> 13:00, 23:32 -> 00:00 on next day)
        let currentDateTime = new Date();
        currentDateTime.setMinutes(0, 0, 0);
        if (currentDateTime.getHours() >= 23) {
            currentDateTime.setDate(currentDateTime.getDate() + 1);
            currentDateTime.setHours(0, 0, 0, 0);
        } else {
            currentDateTime.setHours(currentDateTime.getHours() + 1);
        }
        let isoString = currentDateTime.toISOString();
        isoString = isoString.substring(0, isoString.length - 1);

        this.state = {
            status: 'main',
            error: '',
            title: "",
            datetime: isoString,
            duration: 60,
            description: "",
            validationErrors: ["title"],


            // Because we can change the question type, we keep ALL question's properties in the object, in the case that the question gets changed to the type with the relevant properties. It has the (dis)advantage (depending on how you look at it) that if you change question type, then change back, the properties will be maintained.
            questions: [
                {
                    type: 'open',
                    title: 'Any other comments?',
                    value: '',
                    min: 0,
                    max: 10,
                    choices: ["Option 1", "Option 2"],
                    allowMultiple: false,
                    validationErrors: []
                },
                {
                    type: 'numeric',
                    title: 'On a scale of 1 to 10, 10 being the best, how would you rate the event?',
                    min: 1,
                    max: 10,
                    choices: ["Option 1", "Option 2"],
                    allowMultiple: false,
                    validationErrors: []
                }
            ]
        };
    }

    // Template
    defaultQuestion = {
        type: 'open',
        title: 'Text Feedback',
        value: '',
        min: 0,
        max: 10,
        choices: ["Option 1", "Option 2"],
        allowMultiple: false,
        validationErrors: []
    }

    // Adds a question to the list when add question button pressed
    addQuestion = (e) => {
        e.preventDefault();
        this.setState(state => {
            // Get index of the new question
            let question = {}
            // Copy and modify the template question
            Object.assign(question, this.defaultQuestion);
            question.choices = question.choices.slice(0);
            let newQuestions = state.questions.slice();
            newQuestions.push(question);
            return { questions: newQuestions }
        });
    }

    changeEventProp(prop, e) {
        let newValidationErrors = this.state.validationErrors.slice(0).filter(e => e != prop);
        if (prop == "title") {
            if (e.target.value.trim().length == 0) {
                newValidationErrors.push("title");
            }
        }
        else if (prop == "duration") {
            if (parseInt(e.target.value) < 5) {
                newValidationErrors.push("duration");
            }
        }
        this.setState({
            [prop]: e.target.value,
            validationErrors: newValidationErrors
        });
    }

    render() {
        if (this.state.status == 'error') {
            return (
                <div>
                    <h1>An error occured!: {this.state.error}</h1>
                </div>
            );
        }
        let hasErrors = false;
        if (this.state.validationErrors.length > 0 || this.state.questions.length == 0) {
            hasErrors = true;
        }
        for (let qid in this.state.questions) {
            let q = this.state.questions[qid];
            if (q.validationErrors.length > 0) {
                hasErrors = true;
            }
        }
        return (
            <div className="text-left align-middle pt-2">
                <h1 className="text-center">Create Event</h1>
                <hr />
                <Form onSubmit={this.submit}>
                    <Row>
                        <Col />
                        <Col xs={12} sm={12} md={10} lg={6}>
                            {/* Title */}
                            <Form.Group>
                                <Form.Label className="">Title</Form.Label>
                                <Form.Control className={this.state.validationErrors.includes("title") ? 'border-danger' : ''} type="text" name="title" placeholder="Event Title" onChange={(e) => this.changeEventProp("title", e)} />
                            </Form.Group>

                            {/* Date and Time */}
                            <Form.Row>
                                <Form.Group as={Col} xs={12} sm={6} lg={7}>
                                    <Form.Label>Start Date/Time</Form.Label>
                                    <Form.Control type="datetime-local" value={this.state.datetime} onChange={(e) => this.changeEventProp("datetime", e)} />
                                </Form.Group>
                                <Form.Group as={Col} xs={12} sm={6} lg={5}>
                                    <Form.Label className="w-100">Duration<span className="float-right text-danger">{this.state.validationErrors.includes("duration") ? 'Must be at least 5 mins' : ''}</span></Form.Label>
                                    <InputGroup>
                                        <Form.Control type="number" min="5" className={this.state.validationErrors.includes("duration") ? 'border-danger' : ''} value={this.state.duration} onChange={(e) => this.changeEventProp("duration", e)} />
                                        <InputGroup.Append>
                                            <InputGroup.Text id="startDatePrepend">
                                                <span className="d-none d-md-block">minutes</span>
                                                <span className="d-block d-md-none">mins</span>
                                            </InputGroup.Text>
                                        </InputGroup.Append>
                                    </InputGroup>
                                </Form.Group>
                            </Form.Row>

                            {/* Description */}
                            <Form.Group>
                                <Form.Label>Description</Form.Label>
                                <Form.Control as="textarea" name="description" value={this.state.description} placeholder="Event Description" onChange={(e) => (this.setState({ description: e.target.value }))} />
                            </Form.Group>
                            <hr />

                            {this.renderQuestions()}

                            {/* Buttons */}
                            <Button className="m-2" type="button" variant="primary" onClick={this.addQuestion} disabled={this.state.status === 'completed'}>Add Question</Button>
                            <Button className="m-2" type="button" variant="success" onClick={this.submit} disabled={this.state.status === 'completed' || hasErrors}>Create Event</Button>

                        </Col>
                        <Col />
                    </Row>

                </Form>
            </div>
        );
    }


    submit(e) {

        let startTime = this.state.datetime;
        if (!(startTime instanceof Date)) {
            startTime = new Date(startTime);
        }
        startTime = startTime.getTime();
        let obj_to_send = {
            title: this.state.title,
            startTime: startTime,
            duration: this.state.duration,
            questions: this.state.questions
        };
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + `/api/events`, {
            method: "POST",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(obj_to_send),
            credentials: "include"
        }).then(e => e.json()).then(data => {
            if (data.status == 'success') {
                this.setState({
                    status: 'success',
                    eventCode: data.data.eventCode
                });
                this.props.history.push(`/event/${data.data.eventCode}`);
            }
            else {
                this.setState({
                    status: 'error',
                    error: data.message
                })
            }
        }).catch(e => {
            this.setState({
                status: 'error'
            })
        });
    }

    // Changes a particular question property e.g. title, question type, etc.
    changeQuestionProp(id, prop, e) {
        let question = this.state.questions[id];
        let newValue = e.target.value;
        question.validationErrors = [];
        if (prop == "title") {
            if (newValue.trim().length == 0) {
                question.validationErrors.push("title");
            }
        }
        if (prop == "allowMultiple") {
            newValue = e.target.checked;
        }
        if (prop == "min" || prop == "max") {
            newValue = parseInt(newValue);
            if (isNaN(newValue)) newValue = 0;
            if (prop == "min") {
                if (question.max <= newValue) {
                    question.validationErrors.push("minmax");
                }
            }
            else if (prop == "max") {
                if (question.min >= newValue) {
                    question.validationErrors.push("minmax");
                }
            }
        }
        if (prop == "type") {
            if (newValue == "choice") {
                this.validateChoiceQuestion(question);
            }
        }
        question[prop] = newValue;
        this.setState(this.state);
    }

    deleteQuestion(i) {
        let copy = this.state.questions.slice();

        if (copy.length > i) {
            copy.splice(i, 1);
            this.setState({
                questions: copy
            });
        }
    }

    renderQuestions() {
        let questions = [];
        for (let i in this.state.questions) {
            let question = this.state.questions[i];
            question.id = i;

            let html = null;
            switch (question.type) {
                case 'open':
                    html = this.renderOpenQuestion(question);
                    break;
                case 'numeric':
                    html = this.renderNumericQuestion(question);
                    break;
                case 'choice':
                    html = this.renderChoiceQuestion(question);
                    break;
            }
            let errorClass = "";
            if (question.validationErrors.includes("title")) {
                errorClass = "border-danger";
            }

            questions.push(
                <div>
                    <Row className="text-left">
                        <Col xs={12} sm={6} md={8}>
                            <Form.Group>
                                <Form.Label>Question Title</Form.Label>
                                <Form.Control className={"w-100 " + errorClass} value={question.title} onChange={(e) => this.changeQuestionProp(i, "title", e)} />
                            </Form.Group>
                        </Col>
                        <Col xs={12} sm={6} md={4}>
                            <Form.Group>
                                <Form.Label>Type</Form.Label>
                                <Form.Control as="select" className="w-100" value={question.type} onChange={(e) => this.changeQuestionProp(i, "type", e)}>
                                    <option value="open">Open</option>
                                    <option value="choice">Choice</option>
                                    <option value="numeric">Numeric</option>
                                </Form.Control>
                            </Form.Group>
                        </Col>
                    </Row>
                    {html}
                    <div className="w-100 clearfix">
                        <Button variant="danger" className="ml-auto float-right" onClick={(e) => this.deleteQuestion(i)}>Delete Question</Button>
                    </div>
                    <hr />
                </div>
            );
        }
        return (
            <div className="py-2">
                <h2>Questions</h2>
                <hr />
                {questions}
            </div >
        );
    }

    changeQuestion(id) {
        let func = (e) => {
            let questions = this.state.questions;
            questions[id].value = e.target.value;
            this.setState({
                questions: questions
            });
        };
        return func.bind(this);
    }



    // If it's an open question, don't render anything else 

    renderOpenQuestion(question) {
        return null;
    }

    renderNumericQuestion(question) {
        let error = question.validationErrors.includes("minmax");
        let classes = "";
        if (error) {
            classes = "border-danger";
        }
        return (
            <Row className="text-left">
                <Col xs={12} sm={6}>
                    <Form.Group>
                        <Form.Label>Minimum Value</Form.Label>
                        <Form.Control className={classes} type="number" value={question.min || 0} onChange={(e) => this.changeQuestionProp(question.id, "min", e)} />
                    </Form.Group>
                </Col>
                <Col xs={12} sm={6}>
                    <Form.Label>Maximum Value</Form.Label>
                    <Form.Control className={classes} type="number" value={question.max || 10} onChange={(e) => this.changeQuestionProp(question.id, "max", e)} />
                </Col>
            </Row>
        )
    }


    validateChoiceQuestion(question) {
        let vals = {};
        for (let i in question.choices) {
            let value = question.choices[i];
            if (vals[value] == null) {
                vals[value] = [];
            }
            vals[value].push(i);
        }
        let repeated = [];
        for (let val in vals) {
            if (vals[val].length > 1) {
                repeated = repeated.concat(vals[val]);
            }
        }
        question.validationErrors = [];
        if (repeated.length > 0) {
            question.validationErrors.push('repeated');
            question.repeatedValues = repeated;
        }
    }

    changeOptionForChoiceQuestion(questionIndex, choiceIndex, e) {
        let question = this.state.questions[questionIndex];
        question.choices[choiceIndex] = e.target.value;
        this.validateChoiceQuestion(question);
        this.setState(this.state);
    }
    removeOptionFromChoiceQuestion(questionIndex, choiceIndex) {
        let question = this.state.questions[questionIndex];
        if (question.choices.length > 2) {
            question.choices.splice(choiceIndex, 1);
            this.validateChoiceQuestion(question);
            this.setState(this.state);
        }
    }
    addOptionToChoiceQuestion(questionIndex) {
        let question = this.state.questions[questionIndex];
        question.choices.push("Option " + (question.choices.length + 1).toString());
        this.validateChoiceQuestion(question);
        this.setState(this.state);
    }

    renderChoiceQuestion(question) {
        let options = [];
        for (let i in question.choices) {
            let choice = question.choices[i];
            let disabled = question.choices.length <= 2;
            let errorClass = "";
            if (question.validationErrors.includes("repeated")) {
                if (question.repeatedValues.includes(i.toString())) {
                    errorClass = "border-danger";
                }
            }
            options.push(
                <div className="d-flex mb-1 ">
                    <Form.Control className={"flex-grow-1 " + errorClass} value={choice} onChange={(e) => this.changeOptionForChoiceQuestion(question.id, i, e)} />
                    <Button disabled={disabled} variant="danger" onClick={(e) => this.removeOptionFromChoiceQuestion(question.id, i)}>Delete</Button>
                </div>
            );
        }
        return (
            <div className="text-left">
                <div className="clearfix">
                    <Button className="mb-1" variant="info" onClick={(e) => this.addOptionToChoiceQuestion(question.id)}>Add Option</Button>
                    <div className="float-right">
                        <Form.Check
                            checked={question.allowMultiple}
                            onChange={(e) => this.changeQuestionProp(question.id, "allowMultiple", e)}
                            type="checkbox"
                            id={`allow-multiple-checkbox-${question.id}`}
                            label="Allow users to check multiple options?"
                        />
                    </div>

                </div>
                {options}
            </div>
        )
    }

}
import React from "react";
import { Form, Col, Row, Button } from "react-bootstrap";

export default class CreateEventView extends React.Component{
    constructor(props) {
        super(props);

        // Currently copy and paste from feedback
        this.state = {
            loading: false,
            anonymous: false,
            questions: {
                "0": {
                    id: "0", // this needs to be the same as the index, so the questions can directly access their own state
                    type: 'open',
                    title: 'Any other comments?',
                    value: ''
                },
                "1": {
                    id: "1",
                    type: 'numeric',
                    title: 'On a scale of 1 to 10, 10 being the best, how would you rate the event?',
                    min: 1,
                    max: 10,
                    value: 5
                }
            }
        };
    }

    // Template of
    defaultQuestion = {
        id: "0",
        type: 'open',
        title: 'Text Feedback',
        value: ''
    }

    // Adds a question to the list when add question button pressed
    // TODO Pretty sure this is the wrong way to update a dict in setState, but couldn't find better way immediately
    addQuestion = (e) => {
        e.preventDefault();
        this.setState(state => {
            const index = (Object.keys(state.questions).length + 1).toString();
            var question = {}
            Object.assign(question, this.defaultQuestion);
            question.id = index;
            const questions = state.questions;
            questions[index] = question;
            return {questions}
        });
        console.log(this.state);
    }

    render() {
        return (
            <div className="text-center align-middle">
                <h1>CREATE EVENT</h1>
                <Form>
                    <div className="float-right">
                        <Form.Label>Time</Form.Label>
                        <Form.Control className="form-control" type="text" name="time" placeholder="[PH] Time here"/>
                    </div>

                    <Form.Label className="w-75">Title</Form.Label>
                    <Form.Control className="form-control w-75" type="text" name="title" placeholder="Event"/>


                    <Form.Label className="pt-2">Description</Form.Label>
                    <textarea className="form-control " name="description" placeholder="Enter event description here"/>

                    <hr />
                    <input className="btn btn-primary m-2" type="button" value="Add Question" onClick={this.addQuestion}/>
                    <input className="btn btn-primary m-2" type="submit" value="CREATE" />

                    {this.renderQuestions()}
                </Form>
            </div>
        );
    }

    renderQuestions() {
        let questions = [];
        for (let id in this.state.questions) {
            let question = this.state.questions[id];
            switch (question.type) {
                case 'open':
                    questions.push(this.renderLongQuestion(question));
                    break;
                case 'numeric':
                    questions.push(this.renderRangeQuestion(question));
                    break;
                case 'choice':
                    questions.push(this.renderMultipleChoiceQuestion(question));
                    break;
            }
            questions.push(<hr />);
        }
        return (
            <div className="text-center py-2">
                <b>[PLACEHOLDER] Questions</b>
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

    // TODO Change titles to text boxes and have multiple choice for type

    renderLongQuestion(question) {
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" onChange={this.changeQuestion(question.id)} value={question.value} placeholder="Enter answer here" as="textarea" rows={4}></Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        );
    }

    renderRangeQuestion(question) {
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>

                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" value={question.value} onChange={this.changeQuestion(question.id)} type="range" min={question.min} max={question.max} step="1"></Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        )
    }

    renderMultipleChoiceQuestion(question) {
        let options = [];
        for (let choice in question.choices) {
            options.push(<option value={choice}>{question.choices[choice]}</option>);
        }
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" as="select" value={question.value} onChange={this.changeQuestion(question.id)}>
                            <option disabled selected value={-1}>Choose an option...</option>
                            {options}
                        </Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group>
        )
    }

}
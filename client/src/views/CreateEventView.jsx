import React from "react";
import { Form, Col, Row, Button } from "react-bootstrap";

export default class CreateEventView extends React.Component{
    constructor(props) {
        super(props);

        // When changing questions are added, need to remove value field
        this.state = {
            title: "",
            date: "",
            time: "",
            description: "",

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

    // Template
    defaultQuestion = {
        id: "0",
        type: 'open',
        title: 'Text Feedback',
        value: ''
    }

    // Adds a question to the list when add question button pressed
    addQuestion = (e) => {
        e.preventDefault();
        this.setState(state => {
            // Get index of the new question
            const index = (Object.keys(state.questions).length + 1).toString();
            let question = {}
            // Copy and modify the template question
            Object.assign(question, this.defaultQuestion);
            question.id = index;
            // Copy and modify the questions list (must return a separate object, as these events could theoretically be ran asynchronously)
            const questions = {...state.questions};
            questions[index] = question;
            return {questions}
        });
        console.log(this.state);
    }

    render() {
        return (
            <div className="text-center align-middle pt-2">
                <h1>Create Event</h1>
                <hr />
                <Form method="POST" id="event_create" action="/test/eventCreate">
                <Row>
                    <Col/>
                    <Col xs={12} sm={10} md={6}>
                        {/* Title */}
                        <Form.Group className="m-2">
                            <Form.Label className="">Title</Form.Label>
                            <Form.Control type="text" name="title" placeholder="Event" onChange={(e) => (this.setState({title: e.target.value}))}/>
                        </Form.Group>

                        {/* Date and Time */}
                        <Form.Row>
                            <Col className="m-2">
                                <Form.Label>Date</Form.Label>
                                <Form.Control type="date" name="date" placeholder="Date" onChange={(e) => (this.setState({date: e.target.value}))}/>
                            </Col>
                            <Col className="m-2">
                                <Form.Label>Time</Form.Label>
                                <Form.Control type="time" name="time" placeholder="Time" onChange={(e) => (this.setState({time: e.target.value}))}/>
                            </Col>
                        </Form.Row>

                        {/* Description */}
                        <Form.Label className="pt-2 m-2">Description</Form.Label>
                        <Form.Control as="textarea" name="description" placeholder="Enter event description here" onChange={(e) => (this.setState({description: e.target.value}))}/>
                        <hr />

                        {/* Buttons */}
                        <Button className="m-2" type="button" variant="primary" onClick={this.addQuestion}>Add Question</Button>
                        <Button className="m-2" type="submit" variant="primary" >CREATE</Button>

                        {/* Temporary question render */}
                        {this.renderQuestions()}
                    </Col>
                    <Col/>
                </Row>

                </Form>
            </div>
        );
    }

    submit(e) {
        e.preventDefault();
        fetch(this.props.formAction, {
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({description: this.state.description})
        });
    }

    // TODO Temporary rendering of questions

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
                    <Form.Control className="mx-auto" onChange={this.changeQuestion(question.id)} value={question.value} placeholder="Enter answer here" as="textarea" rows={4}/>
            </Form.Group >
        );
    }

    renderRangeQuestion(question) {
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Form.Control className="mx-auto" value={question.value} onChange={this.changeQuestion(question.id)} type="range" min={question.min} max={question.max} step="1"/>
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
                <Form.Control className="mx-auto" as="select" value={question.value} onChange={this.changeQuestion(question.id)}>
                    <option disabled selected value={-1}>Choose an option...</option>
                    {options}
                </Form.Control>
            </Form.Group>
        )
    }

}
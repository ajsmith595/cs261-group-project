import React from "react";
import { Form, Col, Row, Button } from "react-bootstrap";
<<<<<<< HEAD
=======
import Multiselect from "react-multi-select-component";
>>>>>>> feature/feedback-view
// import '../feedback.css';
export default class FeedbackView extends React.Component {
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);


        /* 
        TODO: make it so that when the page is accessed, loading is 'true'.
        Then, in the componentDidMount method (built in React method), do a GET request to the /api/event/:id (by using fetch method, or similar), and populate the 'questions' state (use:
            this.setState({questions: data, loading: false})
        ).
        Then, when the form button is clicked, do a POST request to the /api/event/:id/feedback (not 100% sure on the URL) with the current state.
        */
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
                },
                "2": {
                    id: "2",
                    type: 'choice',
                    title: 'Choose a colour',
                    choices: ["Red", "Yellow", "Green", "Blue"],
                    value: -1
                },
                "3": {
                    id: "3",
                    type: "multi-choice",
                    title: "choose all colours",
                    choices: ["Red", "Yellow", "Blue", "Orange", "Marshmellow"],
                    value: []
                }
            }
        };
    }

    /*componentDidMount() {
        fetch('/api/event/:id')
          .then((response) => response.json())
          .then((data) => this.setState({questions: data, loading: false}));
    }*/

    sendStateToServer() {
        fetch('/api/event/:id/feedback',
        {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.state),
        }
        ).then(response => {
            if (response.status >= 400) {
                throw new Error("Bad response from server");
            }
            response.json().then(data => {
              console.log("success");
            })
        })
    }

    render() {
        if (this.state.loading) {
            return (
                <h1 className="text-center">Loading...</h1>
            );
        }
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
<<<<<<< HEAD
=======
                    questions.push(this.renderChoiceQuestion(question));
                    break;
                case 'multi-choice':
>>>>>>> feature/feedback-view
                    questions.push(this.renderMultipleChoiceQuestion(question));
                    break;
            }
            questions.push(<hr />);
        }
        return (
            <div className="text-center py-2">
                <h1>Feedback</h1>
                <hr />
                <Form method="POST" id="feedback" action="/event/attendee">
                    {questions}
                    <Form.Check type="checkbox" id="anonymous_check" >
                        <Form.Check.Input type="checkbox" checked={this.state.anonymous} onChange={(e) => this.setState({ anonymous: e.target.checked })} />
                        <Form.Check.Label>Submit the feedback anonymously (the host will not see your username)</Form.Check.Label>
                    </Form.Check>
                    <hr />
                    <Row>

                        <Col xs={0} sm={1} md={3}></Col>
                        <Col xs={12} sm={10} md={6}>
                            <Button className="w-100" type="button" variant="primary" onClick={this.sendStateToServer}>Submit</Button>
                        </Col>
                        <Col xs={0} sm={1} md={3}></Col>
                    </Row>
                </Form>
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
<<<<<<< HEAD
=======
    
    changeMultiQuestion(id) {
        let func = (e) => {
            let questions = this.state.questions;
            questions[id].value = e;
            this.setState({
                questions: questions
            });
        };
        return func.bind(this);
    }
>>>>>>> feature/feedback-view

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
                        <small class="text-center text-muted">{question.value}</small>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        )
    }

<<<<<<< HEAD
    renderMultipleChoiceQuestion(question) {
=======
    renderChoiceQuestion(question) {
>>>>>>> feature/feedback-view
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
<<<<<<< HEAD

        
    }
=======
    }

    renderMultipleChoiceQuestion(question) {
        let options = [];
        for (let choice in question.choices) {
            options.push({label: question.choices[choice], value: question.choices[choice]});
        }
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Multiselect options={options} value={this.state.questions[question.id].value} onChange={this.changeMultiQuestion(question.id)} />
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group>
        )
    }

>>>>>>> feature/feedback-view
}

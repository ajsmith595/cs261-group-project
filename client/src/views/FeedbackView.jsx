import React from "react";
import { Form, Col, Row, Button } from "react-bootstrap";
import Multiselect from "react-multi-select-component";
// import '../feedback.css';
export default class FeedbackView extends React.Component {
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);
        this.state = {
            status: 'show',
            anonymous: false,
            // questions: {
            //     "0": {
            //         id: "0", // this needs to be the same as the index, so the questions can directly access their own state
            //         type: 'open',
            //         title: 'Any other comments?',
            //         value: ''
            //     },
            //     "1": {
            //         id: "1",
            //         type: 'numeric',
            //         title: 'On a scale of 1 to 10, 10 being the best, how would you rate the event?',
            //         min: 1,
            //         max: 10,
            //         value: 5
            //     },
            //     "2": {
            //         id: "2",
            //         type: 'choice',
            //         title: 'Choose a colour',
            //         choices: ["Red", "Yellow", "Green", "Blue"],
            //         value: -1
            //     },
            //     "3": {
            //         id: "3",
            //         type: "multi-choice",
            //         title: "choose all colours",
            //         choices: ["Red", "Yellow", "Blue", "Orange", "Marshmellow"],
            //         value: []
            //     }
            // }
            questions: props.data.questions
        };


    }

    //When the page has received the form from the server it loads the page
    /*componentDidMount() {
        fetch('/api/event/:id')
          .then((response) => response.json())
          .then((data) => this.setState({questions: data, loading: false}));
    }*/

    /*
    Sends the state to the server to store and analyse the feedback
    */
    sendStateToServer() {
        this.setState({
            status: 'loading'
        });
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + `/api/event/${this.props.eventID}/feedback`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(this.state),
                credentials: "include"
            }
        ).then(response => response.json()).then(data => {
            if (data.status === 'success') {
                // Reset all the values of all the questions, so if they want to submit again, the question values are not identical
                for (let i in this.state.questions) {
                    let question = this.state.questions[i];

                    if (question.type === "open") {
                        question.value = "";
                    }
                    else if (question.type === "choice") {
                        if (question.multiple) {
                            question.value = [];
                        }
                        else {
                            question.value = -1;
                        }
                    }
                    else if (question.type === "numeric") {
                        question.value = Math.round((question.min + question.max) / 2);
                    }
                }
                this.setState({
                    status: 'success',
                    questions: this.state.questions
                });
            }
            else {
                this.setState({
                    status: 'error'
                })
            }
        }).catch(err => {
            this.setState({
                status: 'error'
            })
        });
    }

    /*
    Renders the feedback-view
    */
    render() {
        if (this.state.status === 'loading') {
            return (
                <h1 className="text-center">Loading...</h1>
            );
        }
        else if (this.state.status === 'success') {
            return (
                <Row>
                    <Col xs={0} sm={1} md={3} />
                    <Col xs={12} sm={10} md={6}>
                        <h1 className="text-center">Feedback Successful!</h1>
                        <Button className="w-100 mx-auto" variant="primary" onClick={() => this.setState({ status: 'show' })}>Give more feedback</Button>
                    </Col>
                    <Col xs={0} sm={1} md={3} />
                </Row>
            );
        }
        else if (this.state.status === 'error') {
            return (
                <Row>
                    <Col xs={0} sm={1} md={3} />
                    <Col xs={12} sm={10} md={6}>
                        <h1 className="text-center">An error occurred!</h1>
                        <Button className="w-100 mx-auto" variant="primary" onClick={() => this.setState({ status: 'show' })}>Try Again?</Button>
                    </Col>
                    <Col xs={0} sm={1} md={3} />
                </Row>
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
                    if (!question.multiple) {
                        questions.push(this.renderChoiceQuestion(question));
                    } else {
                        questions.push(this.renderMultipleChoiceQuestion(question));
                    }
                    break;
            }
            questions.push(<hr />); //Adds a line after every question
        }
        return (
            <div className="text-center py-2">
                <h1>Feedback</h1>
                <hr />
                <Form onSubmit={this.sendStateToServer}>
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

    /*
    Changes the given questions value based on given ID
    And which element called the function
    */
    changeQuestion(id) {
        let func = (e) => {
            let questions = this.state.questions;
            questions[id].value = e.target.value; //Gets the value of the caller
            this.setState({
                questions: questions
            });
        };
        return func.bind(this);
    }

    /*
    Changes the given questions value based on given ID
    for the 'multiple-choice question type'
    */
    changeMultiQuestion(id) {
        let func = (e) => {
            let questions = this.state.questions;
            questions[id].value = e; //Requires just e rather than e.target.value
            this.setState({
                questions: questions
            });
        };
        return func.bind(this);
    }

    /*
    Renders the long form text question
    Includes a 'text-area' to enter the response
    */
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

    /*
    Renders the long form text question
    Includes a range slider to enter the response
    and shows the value of the response to the user
    */
    renderRangeQuestion(question) {
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>

                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" value={question.value} onChange={this.changeQuestion(question.id)} type="range" min={question.min} max={question.max} step="1"></Form.Control>
                        <small className="text-center text-muted">{question.value}</small>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        )
    }

    /*
    Renders the long form text question
    Gives the user a list of options to choose from
    The user can only choose at most one response
    */
    renderChoiceQuestion(question) {
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
                            <option disabled value={-1}>Choose an option...</option>
                            {options}
                        </Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group>
        )
    }

    /*
    Renders the long form text question
    Gives the user a list of options to choose from
    The user can choose any amount of answers
    */
    renderMultipleChoiceQuestion(question) {
        let options = [];
        for (let choice in question.choices) {
            options.push({ label: question.choices[choice], value: question.choices[choice] });
        }
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Multiselect className="text-left" options={options} value={this.state.questions[question.id].value} onChange={this.changeMultiQuestion(question.id)} />
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group>
        )
    }

}

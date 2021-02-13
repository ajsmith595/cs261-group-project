import React from "react";
import '../feedback.css';
export default class FeedbackView extends React.Component{
    render(){
        return (
            <div>
                <noscript>You need to enable JavaScript to run this app.</noscript>
                <nav class="navbar">
                    <div class="container-fluid">
                    <button class="btn btn-primary"><a>Back</a></button>
                    <div class="d-flex">
                        <button class="btn btn-dark"><a>Logout</a></button>
                    </div>
                    </div>
                </nav>
                <div class="box">
                    <div id="feedback-form">
                        <h2>Feedback</h2>
                        <hr/>
                        <form method="POST" id="feedback" action="/test/eventForm">
                            {this.renderLongQuestion("Any other comments?")}
                            {this.renderRangeQuestion("On a scale of 1 to 10, 10 being the best, how would you rate the event?")}  
                            {this.renderMultipleChoiceQuestion()}   
                            <input type="checkbox" name="anonymous"/> <p>Submit the feedback anonymously (the host will not see your username)</p>
                            <hr/>
                            <div><input class="btn btn-primary long-button" type="submit" value="Submit" /></div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }

    renderLongQuestion(question) {
        return (
            <div>
                <p>{question}</p>
                <textarea class="feedback-text" name="longAnswer" rows="4" value="" placeholder="Enter answer here"></textarea>
                <hr/>
            </div>
        );
    }

    renderRangeQuestion(question) {
        return (
            <div>
                <p>{question}</p>
                <input type="range" class="rangeAnswer" name="rangeAnswer" min="1" max="10" step="1"/>
                <hr/>
            </div>
        )
    }

    renderMultipleChoiceQuestion(){
        return (
            <div>
                <label name="multipleAnswer" for="colour">Choose a colour:</label>
                <select id="colour">
                <option value="red">Red</option>
                <option value="yellow">Yellow</option>
                <option value="blue">Blue</option>
                <option value="green">Green</option>
                </select>    
                <hr/>
            </div>
        )
    }
}
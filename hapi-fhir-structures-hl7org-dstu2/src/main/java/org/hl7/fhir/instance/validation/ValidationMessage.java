package org.hl7.fhir.instance.validation;

/*
 Copyright (c) 2011+, HL7, Inc
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
 this list of conditions and the following disclaimer in the documentation 
 and/or other materials provided with the distribution.
 * Neither the name of HL7 nor the names of its contributors may be used to 
 endorse or promote products derived from this software without specific 
 prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.

 */

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.instance.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.instance.model.StringType;
import org.hl7.fhir.instance.model.valuesets.IssueType;
import org.hl7.fhir.instance.utils.ToolingExtensions;
import org.hl7.fhir.utilities.Utilities;

public class ValidationMessage {
  
  //@formatter:off
  public enum Source {
    ExampleValidator, 
    ProfileValidator, 
    ResourceValidator, 
    InstanceValidator, 
    Schema, 
    Schematron, 
    Publisher, 
    Ontology, 
    ProfileComparer, 
    QuestionnaireAnswersValidator
  }
  //@formatter:on

  private Source source;
  private int line;
  private int col;
  private String location;
  private String message;
  private IssueType type;
  private IssueSeverity level;
  private String html;

  public ValidationMessage(Source source, IssueType type, String path, String message, IssueSeverity level) {
    super();
    this.line = -1;
    this.col = -1;
    this.location = path;
    this.message = message;
    this.html = Utilities.escapeXml(message);
    this.level = level;
    this.source = source;
    this.type = type;
    if (type == null)
      throw new Error("A type must be provided");
  }

  public ValidationMessage(Source source, IssueType type, int line, int col, String path, String message,
      IssueSeverity level) {
    super();
    this.line = line;
    this.col = col;
    this.location = path;
    this.message = message;
    this.html = Utilities.escapeXml(message);
    this.level = level;
    this.source = source;
    this.type = type;
    if (type == null)
      throw new Error("A type must be provided");
  }

  public ValidationMessage(Source source, IssueType type, String path, String message, String html, IssueSeverity level) {
    super();
    this.line = -1;
    this.col = -1;
    this.location = path;
    this.message = message;
    this.html = html;
    this.level = level;
    this.source = source;
    this.type = type;
    if (type == null)
      throw new Error("A type must be provided");
  }

  public ValidationMessage(Source source, IssueType type, int line, int col, String path, String message, String html,
      IssueSeverity level) {
    super();
    this.line = line;
    this.col = col;
    this.location = path;
    this.message = message;
    this.html = html;
    this.level = level;
    this.source = source;
    this.type = type;
    if (type == null)
      throw new Error("A type must be provided");
  }

  public ValidationMessage() {
  }

  public String getMessage() {
    return message;
  }

  public ValidationMessage setMessage(String message) {
    this.message = message;
    return this;
  }

  public IssueSeverity getLevel() {
    return level;
  }

  public ValidationMessage setLevel(IssueSeverity level) {
    this.level = level;
    return this;
  }

  public Source getSource() {
    return source;
  }

  public ValidationMessage setSource(Source source) {
    this.source = source;
    return this;
  }

  public String getLocation() {
    return location;
  }

  public ValidationMessage setLocation(String location) {
    this.location = location;
    return this;
  }

  public IssueType getType() {
    return type;
  }

  public ValidationMessage setType(IssueType type) {
    this.type = type;
    return this;
  }

  public String summary() {
    return level.toString() + " @ " + location
        + (line >= 0 && col >= 0 ? " (line " + Integer.toString(line) + ", col" + Integer.toString(col) + ") " : " ")
        + message + (source != null ? " (src = " + source + ")" : "");
  }

  public OperationOutcomeIssueComponent asIssue(OperationOutcome op) throws Exception {
    OperationOutcomeIssueComponent issue = new OperationOutcome.OperationOutcomeIssueComponent();
    issue.setCode(new CodeableConcept());
    issue.getCode().addCoding().setSystem(type.getSystem()).setCode(type.toCode());
    if (location != null) {
      StringType s = new StringType();
      s.setValue(location
          + (line >= 0 && col >= 0 ? " (line " + Integer.toString(line) + ", col" + Integer.toString(col) + ")" : ""));
      issue.getLocation().add(s);
    }
    issue.setSeverity(level);
    issue.setDetails(message);
    if (source != null) {
      issue.getExtension().add(ToolingExtensions.makeIssueSource(source));
    }
    return issue;
  }
  
  
  /**
   * @return Returns -1 if the value is not set or not known
   */
  public int getLine() {
    return line;
  }

  /**
   * @return Returns -1 if the value is not set or not known
   */
  public int getCol() {
    return col;
  }

  public String toXML() {
    return "<message source=\"" + source + "\" line=\"" + line + "\" col=\"" + col + "\" location=\"" + location
        + "\" type=\"" + type + "\" level=\"" + level + "\"><plain>" + Utilities.escapeXml(message) + "</plain><html>"
        + html + "</html></message>";
  }

  public String getHtml() {
    return html == null ? Utilities.escapeXml(message) : html;
  }

  /**
   * Returns a representation of this ValidationMessage suitable for logging. The values of most of the internal fields are included, so this may not be
   * suitable for display to an end user.
   */
  @Override
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    b.append("level", level);
    b.append("type", type);
    b.append("location", location);
    b.append("message", message);
    return b.build();
  }

}
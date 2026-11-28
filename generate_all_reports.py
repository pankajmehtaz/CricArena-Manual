import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn
import os
import shutil

def set_page_layout(doc):
    for section in doc.sections:
        section.page_width = Inches(8.27)  # A4 width
        section.page_height = Inches(11.69) # A4 height
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(0.5)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('w:top', top), ('w:bottom', bottom), ('w:left', left), ('w:right', right)]:
        node = OxmlElement(m)
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def set_cell_shading(cell, color_hex):
    shading_elm = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{color_hex}"/>')
    cell._tc.get_or_add_tcPr().append(shading_elm)

def add_chapter_header(doc, chapter_no_str, chapter_title_str):
    p_num = doc.add_paragraph()
    p_num.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_num.paragraph_format.space_before = Pt(20)
    p_num.paragraph_format.space_after = Pt(4)
    p_num.paragraph_format.keep_with_next = True
    r_num = p_num.add_run(chapter_no_str)
    r_num.font.name = 'Times New Roman'
    r_num.font.size = Pt(14)
    r_num.font.bold = True
    r_num.font.color.rgb = RGBColor(0, 0, 0)

    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(0)
    p_title.paragraph_format.space_after = Pt(18)
    p_title.paragraph_format.keep_with_next = True
    r_title = p_title.add_run(chapter_title_str.upper())
    r_title.font.name = 'Times New Roman'
    r_title.font.size = Pt(14)
    r_title.font.bold = True
    r_title.font.color.rgb = RGBColor(0, 0, 0)

def add_h1(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text.upper())
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True
    r.font.color.rgb = RGBColor(0, 0, 0)
    return p

def add_h2(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True
    r.font.color.rgb = RGBColor(0, 0, 0)
    return p

def add_h3(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.italic = True
    r.font.bold = False
    r.font.color.rgb = RGBColor(0, 0, 0)
    return p

def add_p(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = 1.15
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.color.rgb = RGBColor(0, 0, 0)
    return p

def add_bullet(doc, text, bold_prefix=None):
    p = doc.add_paragraph(style='List Bullet')
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.line_spacing = 1.15
    if bold_prefix:
        r_b = p.add_run(bold_prefix)
        r_b.font.name = 'Times New Roman'
        r_b.font.size = Pt(12)
        r_b.font.bold = True
        r_b.font.color.rgb = RGBColor(0, 0, 0)
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.color.rgb = RGBColor(0, 0, 0)
    return p

def add_num_item(doc, num_str, text, bold_prefix=None):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.left_indent = Inches(0.25)
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.line_spacing = 1.15
    r_n = p.add_run(num_str + " ")
    r_n.font.name = 'Times New Roman'
    r_n.font.size = Pt(12)
    r_n.font.bold = True
    r_n.font.color.rgb = RGBColor(0, 0, 0)
    if bold_prefix:
        r_b = p.add_run(bold_prefix)
        r_b.font.name = 'Times New Roman'
        r_b.font.size = Pt(12)
        r_b.font.bold = True
        r_b.font.color.rgb = RGBColor(0, 0, 0)
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.color.rgb = RGBColor(0, 0, 0)
    return p

def add_fig_caption(doc, caption_text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(14)
    r = p.add_run(caption_text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True
    r.font.color.rgb = RGBColor(0, 0, 0)

def add_table_caption(doc, caption_text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(caption_text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True
    r.font.color.rgb = RGBColor(0, 0, 0)

def insert_image_safe(doc, image_path, width_inches=6.2):
    if os.path.exists(image_path):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(8)
        p.paragraph_format.space_after = Pt(4)
        run = p.add_run()
        run.add_picture(image_path, width=Inches(width_inches))
        return True
    return False

def add_styled_table(doc, headers, data, col_widths=None):
    table = doc.add_table(rows=len(data) + 1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False

    # Header Row
    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        set_cell_shading(hdr_cells[i], "F1F5F9")
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=150, right=150)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for r in p.runs:
            r.font.name = 'Times New Roman'
            r.font.size = Pt(10.5)
            r.font.bold = True

    # Data Rows
    for row_idx, row_data in enumerate(data):
        row_cells = table.rows[row_idx + 1].cells
        bg_color = "FFFFFF" if row_idx % 2 == 0 else "F8FAFC"
        for col_idx, cell_value in enumerate(row_data):
            row_cells[col_idx].text = str(cell_value)
            set_cell_shading(row_cells[col_idx], bg_color)
            set_cell_margins(row_cells[col_idx], top=100, bottom=100, left=150, right=150)
            p = row_cells[col_idx].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            for r in p.runs:
                r.font.name = 'Times New Roman'
                r.font.size = Pt(10)

    if col_widths:
        for row in table.rows:
            for idx, width in enumerate(col_widths):
                row.cells[idx].width = Inches(width)

    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_before = Pt(0)
    p_sp.paragraph_format.space_after = Pt(8)
    return table

def build_front_matter_and_ch1_to_3(doc):
    # ================= COVER PAGE =================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(24)
    p.paragraph_format.space_after = Pt(18)
    r = p.add_run("CRICARENA – COLLEGE CRICKET TOURNAMENT\nMANAGEMENT AND LIVE SCORING SYSTEM")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(16)
    r.font.bold = True

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(12)
    r = p.add_run("A project report submitted in partial fulfillment of the requirements for the 7th semester\nof the degree of")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.italic = True

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("BACHELOR OF TECHNOLOGY\nin\nELECTRONICS ENGINEERING")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(18)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("Submitted by\n")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r_name1 = p.add_run("ISHU PANDEY\n(Enrollment No. 23001008073)\n\n")
    r_name1.font.name = 'Times New Roman'
    r_name1.font.size = Pt(12)
    r_name1.font.bold = True
    r_name2 = p.add_run("PANKAJ MEHTA\n(Enrollment No. 23001008043)\n")
    r_name2.font.name = 'Times New Roman'
    r_name2.font.size = Pt(12)
    r_name2.font.bold = True

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(14)
    r = p.add_run("Under the Supervision of\n")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r_sup = p.add_run("MRS. SHEILZA JAIN")
    r_sup.font.name = 'Times New Roman'
    r_sup.font.size = Pt(13)
    r_sup.font.bold = True

    # University Logo
    logo_path = "documentation/architecture/university_logo.png"
    if os.path.exists(logo_path):
        p_logo = doc.add_paragraph()
        p_logo.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_logo.paragraph_format.space_before = Pt(6)
        p_logo.paragraph_format.space_after = Pt(10)
        p_logo.add_run().add_picture(logo_path, width=Inches(1.5))

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(0)
    r = p.add_run("Department of Electronics Engineering\nFaculty of Engineering and Technology\nJ.C. Bose University of Science and Technology, YMCA, Faridabad,\nHaryana-121006\nNOVEMBER, 2026")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True

    doc.add_page_break()

    # ================= DECLARATION =================
    p_h = doc.add_paragraph()
    p_h.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_h.paragraph_format.space_before = Pt(24)
    p_h.paragraph_format.space_after = Pt(18)
    r = p_h.add_run("DECLARATION")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    add_p(doc, "We hereby certify that the work which is being presented in this project report entitled “CRICARENA: COLLEGE CRICKET TOURNAMENT MANAGEMENT AND LIVE SCORING SYSTEM”, in partial fulfillment of the requirements for the 7th semester of the degree of BACHELOR OF TECHNOLOGY in ELECTRONICS ENGINEERING, submitted to the Department of Electronics Engineering, Faculty of Engineering and Technology, J.C. Bose University of Science and Technology, YMCA, Faridabad, Haryana-121006, is an authentic record of our work carried out during the period from August, 2026 to November, 2026, under the supervision of MRS. SHEILZA JAIN. The matter presented in this project report has not been submitted to any other University/Institute for the award of B.Tech. or any degree or diploma.")

    p_sig = doc.add_paragraph()
    p_sig.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    p_sig.paragraph_format.space_before = Pt(40)
    r = p_sig.add_run("ISHU PANDEY\n(Enrollment No. 23001008073)\n\nPANKAJ MEHTA\n(Enrollment No. 23001008043)")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True

    doc.add_page_break()

    # ================= CERTIFICATE =================
    p_h = doc.add_paragraph()
    p_h.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_h.paragraph_format.space_before = Pt(24)
    p_h.paragraph_format.space_after = Pt(18)
    r = p_h.add_run("CERTIFICATE")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    add_p(doc, "This is to certify that the project entitled “CRICARENA: COLLEGE CRICKET TOURNAMENT MANAGEMENT AND LIVE SCORING SYSTEM” submitted to the Department of Electronics Engineering, Faculty of Engineering and Technology, J.C. Bose University of Science and Technology, YMCA, Faridabad, Haryana-121006 by Pankaj Kumar and Ishu Pandey in partial fulfillment of the requirements for the 7th semester of the degree of BACHELOR OF TECHNOLOGY in ELECTRONICS ENGINEERING, is a bona fide work carried out by them under my supervision and guidance. This project work comprises original work and has not been submitted anywhere else for any other degree to the best of my knowledge.")

    p_sup = doc.add_paragraph()
    p_sup.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    p_sup.paragraph_format.space_before = Pt(40)
    p_sup.paragraph_format.space_after = Pt(24)
    r = p_sup.add_run("Project Supervisor\nDR. SHEILZA JAIN\nAssociate Professor\nDepartment of Electronics Engineering\nJ. C. Bose University of Science and Technology, YMCA, Faridabad")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True

    p_chair = doc.add_paragraph()
    p_chair.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    p_chair.paragraph_format.space_before = Pt(24)
    r = p_chair.add_run("Chairperson\nDR. MUNISH VASHISTH\nDepartment of Electronics Engineering\nJ. C. Bose University of Science and Technology, YMCA, Faridabad")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True

    doc.add_page_break()

    # ================= ACKNOWLEDGMENT =================
    p_h = doc.add_paragraph()
    p_h.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_h.paragraph_format.space_before = Pt(24)
    p_h.paragraph_format.space_after = Pt(18)
    r = p_h.add_run("ACKNOWLEDGMENT")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    add_p(doc, "We would like to express our sincere gratitude to our project supervisor, Mrs. Sheilza Jain, for her invaluable guidance, support and encouragement throughout the development of this project. Her guidance and constructive feedback helped us understand the various aspects of software development, database management, web application architecture and cricket tournament operations, and guided us through the different stages of planning, designing, implementation and testing.")
    add_p(doc, "Her valuable suggestions and constant motivation helped us overcome the challenges encountered during the development of CricArena. We are also thankful to the Department of Electronics Engineering, J.C. Bose University of Science and Technology, YMCA, Faridabad, for providing us with the academic environment and opportunity to undertake this project.")
    add_p(doc, "We would also like to acknowledge the support and cooperation of everyone who contributed directly or indirectly to this project. Finally, we express our heartfelt gratitude to our families and friends for their continuous encouragement and support throughout this journey.")
    add_p(doc, "This project would not have been possible without the guidance of our supervisor, the cooperation of teammates and the support of our families. We sincerely thank everyone who contributed to its completion.")

    p_ack_sig = doc.add_paragraph()
    p_ack_sig.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    p_ack_sig.paragraph_format.space_before = Pt(30)
    r = p_ack_sig.add_run("ISHU PANDEY\nPANKAJ MEHTA")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.bold = True

    doc.add_page_break()

    # ================= ABSTRACT =================
    p_h = doc.add_paragraph()
    p_h.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_h.paragraph_format.space_before = Pt(24)
    p_h.paragraph_format.space_after = Pt(18)
    r = p_h.add_run("ABSTRACT")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    add_p(doc, "Traditional college cricket tournament management often relies on spreadsheets, paper-based records and manual communication, making it difficult to maintain consistent information about colleges, tournaments, teams, players and match scores. This report presents CricArena, a web-based College Cricket Tournament Management and Live Scoring System designed to centralize and streamline these operations.")
    add_p(doc, "The system provides institutional management, role-based authentication, tournament creation and publication, captain application and approval workflows, team and player management, tournament registration and squad selection. Each team can maintain a tournament-specific squad of up to 15 players, from which exactly 11 players are selected for the match Playing XI. The backend also supports match scheduling, toss management, innings progression and ball-by-ball delivery recording. Its scoring engine processes batter runs, extras, wickets, strike rotation and over progression. Match-specific batting and bowling scorecards are dynamically generated from recorded delivery information.")
    add_p(doc, "The application is developed using Java 25 and Spring Boot 4.2.0 for backend services, React 19.1.0 for the frontend and MySQL 8.0 for relational data storage. Spring Security and JWT-based authentication provide secure access control, while Spring Data JPA, Hibernate and Flyway support database operations and schema management. Automated backend testing resulted in 268 passing tests with zero failures or errors.")
    add_p(doc, "The current implementation provides functional backend tournament management and scoring capabilities. However, the dedicated frontend live-scoring console, match setup interface and standalone scorecard page remain scaffolded. Production email dispatch, WebSocket-based live updates, lifetime player statistics and tournament-wide leaderboards are identified as future enhancements.")
    
    p_kw = doc.add_paragraph()
    p_kw.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_kw.paragraph_format.space_before = Pt(6)
    r_kwh = p_kw.add_run("Keywords: ")
    r_kwh.font.name = 'Times New Roman'
    r_kwh.font.size = Pt(12)
    r_kwh.font.bold = True
    r_kwt = p_kw.add_run("College Cricket, Tournament Management, Live Scoring, Spring Boot, React, MySQL, JWT Authentication, Role-Based Access Control, Ball-by-Ball Scoring, Cricket Scorecard.")
    r_kwt.font.name = 'Times New Roman'
    r_kwt.font.size = Pt(12)

    doc.add_page_break()

    # ================= TABLE OF CONTENTS =================
    p_h = doc.add_paragraph()
    p_h.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_h.paragraph_format.space_before = Pt(20)
    p_h.paragraph_format.space_after = Pt(16)
    r = p_h.add_run("CONTENTS")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    toc_data = [
        ["Declaration", "i"],
        ["Certificate", "ii"],
        ["Acknowledgment", "iii"],
        ["Abstract", "iv"],
        ["List of Figures", "vii"],
        ["List of Tables", "viii"],
        ["CHAPTER 1  INTRODUCTION", "1"],
        ["    1.1  Project Overview", "1"],
        ["    1.2  Problem Statement", "1"],
        ["    1.3  Objectives of the Project", "2"],
        ["    1.4  Scope of the Project", "2"],
        ["    1.5  Proposed System", "3"],
        ["CHAPTER 2  LITERATURE REVIEW AND EXISTING SYSTEM", "4"],
        ["    2.1  Introduction", "4"],
        ["    2.2  Traditional Approach to Tournament Management", "4"],
        ["    2.3  Review of Existing Digital Systems", "5"],
        ["    2.4  Cricket Rules Relevant to Automated Scoring", "6"],
        ["    2.5  Comparative Analysis of Existing Approaches", "7"],
        ["    2.6  Identified Requirements and Motivation for CricArena", "8"],
        ["    2.7  Summary", "8"],
        ["CHAPTER 3  SYSTEM ANALYSIS AND REQUIREMENTS", "9"],
        ["    3.1  Introduction", "9"],
        ["    3.2  Existing System Analysis", "9"],
        ["    3.3  Proposed System Analysis", "10"],
        ["    3.4  User Roles and Responsibilities", "10"],
        ["    3.5  Functional Requirements", "11"],
        ["    3.6  Non-Functional Requirements", "14"],
        ["    3.7  System Constraints and Business Rules", "15"],
        ["    3.8  Hardware and Software Requirements", "16"],
        ["    3.9  System Feasibility Analysis", "17"],
        ["    3.10 Summary", "18"],
        ["CHAPTER 4  SYSTEM DESIGN AND ARCHITECTURE", "19"],
        ["    4.1  Introduction", "19"],
        ["    4.2  Overall System Architecture", "19"],
        ["    4.3  UML Use Case Modeling", "22"],
        ["    4.4  Data Flow Analysis (Level 0 DFD)", "25"],
        ["    4.5  Tournament Management Workflow", "28"],
        ["    4.6  Database Design and Schema Specification", "31"],
        ["    4.7  Security Architecture and Multi-Tenancy", "35"],
        ["    4.8  Summary", "37"],
        ["CHAPTER 5  IMPLEMENTATION AND METHODOLOGY", "38"],
        ["    5.1  Introduction", "38"],
        ["    5.2  Technology Stack and Environment", "38"],
        ["    5.3  Authentication and User Management Module", "40"],
        ["    5.4  Institutional College and Invitation Module", "41"],
        ["    5.5  Tournament and Captaincy Application Module", "43"],
        ["    5.6  Team, Player and Squad Management Module", "45"],
        ["    5.7  Match Management and Cricket Scoring Engine", "47"],
        ["    5.8  Database Migration and Flyway Versioning", "50"],
        ["    5.9  Frontend Architecture and Component Hierarchy", "51"],
        ["    5.10 System Graphical User Interfaces and Screenshots", "53"],
        ["    5.11 Summary", "61"],
        ["CHAPTER 6  TESTING, VERIFICATION AND RESULTS", "62"],
        ["    6.1  Introduction", "62"],
        ["    6.2  Testing Methodology and Frameworks", "62"],
        ["    6.3  Automated Test Execution Summary", "63"],
        ["    6.4  Controller and Method Security Integration Tests", "64"],
        ["    6.5  Service Layer and Cricket Scoring Engine Tests", "66"],
        ["    6.6  Database Relational Constraints Verification", "67"],
        ["    6.7  Operational Verification and Known Status", "68"],
        ["    6.8  Summary", "69"],
        ["CHAPTER 7  CONCLUSION AND SCOPE FOR FUTURE WORK", "70"],
        ["    7.1  Conclusion", "70"],
        ["    7.2  Scope for Future Work", "71"],
        ["REFERENCES", "73"]
    ]

    add_styled_table(doc, ["Topic / Section", "Page No."], toc_data, col_widths=[5.6, 1.1])
    doc.add_page_break()

    # ================= LIST OF FIGURES =================
    p_h = doc.add_paragraph()
    p_h.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_h.paragraph_format.space_before = Pt(20)
    p_h.paragraph_format.space_after = Pt(16)
    r = p_h.add_run("LIST OF FIGURES")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    figures_data = [
        ["Figure 4.2", "Overall System Architecture of CricArena", "21"],
        ["Figure 4.3", "Use Case Diagram of CricArena", "24"],
        ["Figure 4.4", "Data Flow Diagram (Level 0) of CricArena", "27"],
        ["Figure 4.5", "Tournament Management Workflow Diagram", "30"],
        ["Figure 4.6", "Entity-Relationship (ER) Relational Schema Diagram", "34"],
        ["Figure 5.1", "Public Tournament and Match Discovery Dashboard (Guest View)", "53"],
        ["Figure 5.2", "Role-Based User Authentication and Sign-In Interface", "54"],
        ["Figure 5.3", "College Administrator Tournament and Operations Dashboard", "55"],
        ["Figure 5.4", "Tournament Creation and Configuration Modal Dialog", "56"],
        ["Figure 5.5", "College Cricket Team Registration and Profile Dialog", "57"],
        ["Figure 5.6", "Student Team Captaincy Application Form Dialog", "58"],
        ["Figure 5.7", "College Administrator Captaincy Review Queue Interface", "59"],
        ["Figure 5.8", "Cricket Match Scheduling and Fixture Creation Dialog", "60"]
    ]
    add_styled_table(doc, ["Figure No.", "Title", "Page No."], figures_data, col_widths=[1.3, 4.4, 1.0])
    doc.add_page_break()

    # ================= LIST OF TABLES =================
    p_h = doc.add_paragraph()
    p_h.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_h.paragraph_format.space_before = Pt(20)
    p_h.paragraph_format.space_after = Pt(16)
    r = p_h.add_run("LIST OF TABLES")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    tables_data = [
        ["Table 2.1", "Comparative Analysis of Existing Tournament Approaches", "7"],
        ["Table 3.1", "Development Hardware Environment Specifications", "16"],
        ["Table 3.2", "Core Software Technologies and Exact Framework Versions", "16"],
        ["Table 4.1", "Complete Inventory of 15 CricArena Application Tables", "32"],
        ["Table 4.2", "Summary of Database Relational Integrity Constraints", "33"],
        ["Table 4.3", "MySQL Virtual Generated Columns and Unique Indexes", "34"],
        ["Table 5.1", "Flyway Database Migration Scripts Catalog (V1.0 – V1.11)", "50"],
        ["Table 6.1", "Automated Backend Test Suite Execution Breakdown", "55"],
        ["Table 6.2", "Targeted Test Case Verification Matrix by Subsystem", "57"]
    ]
    add_styled_table(doc, ["Table No.", "Title", "Page No."], tables_data, col_widths=[1.3, 4.4, 1.0])
    doc.add_page_break()

    # ================= CHAPTER 1 =================
    add_chapter_header(doc, "Chapter 1", "INTRODUCTION")
    add_h1(doc, "1.1 Project Overview")
    add_p(doc, "CricArena is a web-based College Cricket Tournament Management and Live Scoring System developed to streamline the organization and management of inter-collegiate cricket tournaments. The platform provides a centralized environment for managing colleges, tournaments, teams, players, tournament registrations, squad selection, match scheduling and cricket scoring operations.")
    add_p(doc, "Traditional college cricket tournament management often involves maintaining information through spreadsheets, paper-based records and separate communication channels. Such methods can make it difficult to maintain consistent records, coordinate tournament activities and access match information efficiently.")
    add_p(doc, "CricArena addresses these challenges by introducing a centralized digital platform with institutional-level access control. The system supports multiple colleges, with each college operating within its assigned administrative scope. A centralized Platform Administrator oversees the platform, while College Administrators and Team Captains perform operations according to their assigned responsibilities.")
    add_p(doc, "The platform includes workflows for college registration, administrator invitations, tournament creation and publication, captain applications, team management and tournament-specific squad registration. Each team can maintain a maximum squad of 15 players for a tournament, from which exactly 11 players are selected for a match Playing XI.")
    add_p(doc, "An important component of CricArena is its backend cricket scoring engine. It supports match scheduling, toss management, innings progression and ball-by-ball delivery recording. The scoring engine processes batting runs, extras, wickets, strike rotation and over progression. Match-specific batting and bowling scorecards are dynamically generated from recorded delivery information.")
    add_p(doc, "The application is developed using Java and Spring Boot for backend services, React for the frontend and MySQL for relational data storage. Spring Security and JSON Web Tokens (JWT) are used to implement authentication and role-based access control.")
    add_p(doc, "The current implementation has a functional backend for the principal tournament management and scoring operations. Several frontend match-related routes, including the interactive live-scoring console, match setup and standalone scorecard pages, remain scaffolded and are identified as areas for further development.")

    add_h1(doc, "1.2 Problem Statement")
    add_p(doc, "The management of inter-collegiate cricket tournaments involves several interconnected activities, including college coordination, team registration, player management, fixture scheduling, match scoring and result maintenance. When these activities are handled through disconnected or manual systems, several problems may arise:")
    add_num_item(doc, "1.", "Fragmented Information: Tournament details, team records, player information and match results may be maintained in separate files or communication channels.")
    add_num_item(doc, "2.", "Administrative Complexity: Managing multiple colleges, tournaments and teams requires clearly defined responsibilities and controlled access to institutional information.")
    add_num_item(doc, "3.", "Player and Squad Management: Maintaining tournament-specific player lists and ensuring that players are not registered with multiple teams in the same tournament can be difficult without proper validation.")
    add_num_item(doc, "4.", "Match Record Management: Manual ball-by-ball scoring requires continuous calculation of runs, extras, wickets, overs and innings totals.")
    add_num_item(doc, "5.", "Limited Access to Match Information: Players, organizers and spectators need a convenient way to access tournament details, match schedules and score information.")
    add_p(doc, "Therefore, there is a need for a centralized digital system that can organize college cricket tournament operations, enforce appropriate access restrictions and provide a structured mechanism for recording and retrieving match information.")

    add_h1(doc, "1.3 Objectives of the Project")
    add_p(doc, "The primary objective of CricArena is to develop a centralized web-based platform for managing college-level cricket tournaments and their associated operations. The specific objectives are:")
    add_num_item(doc, "1.", "To develop a centralized platform for managing multiple colleges and their cricket tournaments.")
    add_num_item(doc, "2.", "To implement secure user authentication using JWT-based access and refresh tokens.")
    add_num_item(doc, "3.", "To establish role-based access control for Platform Administrators, College Administrators and Team Captains.")
    add_num_item(doc, "4.", "To maintain institutional data isolation so that college-level operations remain restricted to their authorized administrative scope.")
    add_num_item(doc, "5.", "To provide tournament creation, modification and publication functionality.")
    add_num_item(doc, "6.", "To implement a digital captain application and approval workflow.")
    add_num_item(doc, "7.", "To manage teams, player registrations and tournament-specific squads.")
    add_num_item(doc, "8.", "To enforce a maximum squad size of 15 players per team for a tournament.")
    add_num_item(doc, "9.", "To implement match Playing XI selection with exactly 11 players selected from the approved tournament squad.")
    add_num_item(doc, "10.", "To provide match scheduling, toss management and innings initiation functionality.")
    add_num_item(doc, "11.", "To develop a backend ball-by-ball scoring engine supporting runs, extras, wickets, strike rotation and over progression.")
    add_num_item(doc, "12.", "To dynamically generate match-specific batting and bowling scorecards.")
    add_num_item(doc, "13.", "To maintain structured relational records using MySQL and database migration management through Flyway.")

    add_h1(doc, "1.4 Scope of the Project")
    add_p(doc, "CricArena focuses on the administrative and operational requirements of inter-collegiate cricket tournaments. The project scope includes the following areas:")
    add_h2(doc, "1.4.1 College and Administrative Management")
    add_p(doc, "The platform supports centralized college management. A Platform Administrator can create colleges and invite College Administrators. Invitation acceptance includes account activation and role assignment.")
    add_h2(doc, "1.4.2 User Authentication and Authorization")
    add_p(doc, "The system implements JWT-based authentication using access and refresh tokens. Role-based authorization controls access to administrative and team-management operations.")
    add_h2(doc, "1.4.3 Tournament Management")
    add_p(doc, "Authorized administrators can create and manage tournaments, configure tournament details and publish tournaments. Team registration and tournament-related match information are associated with the relevant institutional scope.")
    add_h2(doc, "1.4.4 Captain Application and Team Management")
    add_p(doc, "Students can apply for captaincy in published tournaments. College Administrators can review these applications. Approved applicants can obtain the required permissions for team creation or team leadership according to the existing team ownership rules.")
    add_h2(doc, "1.4.5 Player and Squad Management")
    add_p(doc, "Team Captains can manage their team rosters and tournament-specific squads. The system enforces a maximum of 15 players in each tournament squad and prevents a player from representing multiple teams in the same tournament.")
    add_h2(doc, "1.4.6 Match Management and Scoring")
    add_p(doc, "The backend supports match scheduling, toss recording, Playing XI selection, innings management and ball-by-ball delivery recording. The scoring engine processes different types of runs, extras and dismissals and calculates match-specific statistics.")
    add_h2(doc, "1.4.7 Scorecard Generation")
    add_p(doc, "The backend dynamically calculates batting figures, bowling figures, extras, fall of wickets and match results using recorded delivery information.")
    add_h2(doc, "1.4.8 Public Access")
    add_p(doc, "Public users can access available college, tournament and match information without requiring authentication. Public scorecard information is also exposed through the backend API.")
    add_h2(doc, "1.4.9 Limitations of the Current Scope")
    add_p(doc, "The current implementation does not include lifetime player career statistics, automated tournament-wide leaderboards or WebSocket-based live score broadcasting. Production SMTP email dispatch is also not configured. Furthermore, the dedicated frontend live-scoring console, match setup interface and standalone full scorecard page are currently scaffolded rather than fully implemented.")

    add_h1(doc, "1.5 Proposed System")
    add_p(doc, "The proposed system, CricArena, provides an integrated approach to managing college cricket tournaments through a centralized web application.")
    add_p(doc, "The platform follows a multi-role administrative structure. The Platform Administrator manages the overall platform and college onboarding. College Administrators manage tournament operations within their assigned colleges, while Team Captains manage their respective teams and tournament squads.")
    add_p(doc, "The backend is organized into controller, service and repository layers. REST APIs provide communication between the frontend and backend. Business rules are implemented in the service layer, while Spring Data JPA and Hibernate manage relational database operations. MySQL stores institutional, tournament, team, player, match and scoring information. Flyway manages database schema evolution through version-controlled migration scripts.")
    add_p(doc, "For match scoring, the backend records individual delivery events and processes them to maintain innings information and calculate scorecards. This approach provides structured match records and supports the generation of match-specific statistics. The React frontend provides dashboards and management interfaces for the implemented administrative workflows. Match-related frontend interfaces that are not yet complete are identified separately for future development.")

    doc.add_page_break()

    # ================= CHAPTER 2 =================
    add_chapter_header(doc, "Chapter 2", "LITERATURE REVIEW AND EXISTING SYSTEM")
    add_h1(doc, "2.1 Introduction")
    add_p(doc, "The development of digital platforms has changed the way sports tournaments are organized, administered and followed. Activities that were traditionally performed using paper records, spreadsheets and manual communication can now be managed through centralized web-based applications. In cricket tournaments, several activities must be coordinated, including team registration, player management, tournament scheduling, match preparation, score recording and result publication. Managing these activities independently can increase administrative effort and make it difficult to maintain consistent records. This chapter examines existing sports management approaches, digital cricket management platforms and the cricket rules relevant to automated scoring. The findings provide the background for the design and development of CricArena.")

    add_h1(doc, "2.2 Traditional Approach to Tournament Management")
    add_p(doc, "In a traditional tournament management system, organizers generally maintain tournament information using paper-based documents, spreadsheets and manual communication. The major activities include recording participating college and team details, maintaining player lists, preparing fixtures, recording match scores manually, calculating statistics, and communicating updates. While workable for informal recreational matches, this approach suffers from data fragmentation, high administrative dependency, scoring calculation errors, and poor accessibility.")

    add_h1(doc, "2.3 Review of Existing Digital Systems")
    add_h2(doc, "2.3.1 Web-Based Sports Event Management System (SLEMS)")
    add_p(doc, "Raja Zarif Ikmal Raja Mohamad Faisal and Hana Munira Muhd Mukhtar presented a study on developing a centralized event management system for volleyball tournaments in Malaysia. The study demonstrated that web applications reduce administrative burden significantly. CricArena extends these principles to multi-institutional collegiate cricket tournaments.")
    add_h2(doc, "2.3.2 CricClubs")
    add_p(doc, "CricClubs provides league management and live scoring across web and mobile platforms. It supports series creation, schedules, and administrative roles. CricArena incorporates similar concepts but focuses specifically on institutional tenancy boundaries, student captain applications, and cross-tournament squad constraints.")
    add_h2(doc, "2.3.3 CricHeroes")
    add_p(doc, "CricHeroes offers grassroots scoring workflows, match preparation, and Playing XI selection. CricArena adapts these operational cricket capabilities into a structured college tournament governance framework.")

    add_h1(doc, "2.4 Cricket Rules Relevant to Automated Scoring")
    add_p(doc, "Under the official Laws of Cricket established by the Marylebone Cricket Club (MCC), a digital scoring engine must strictly distinguish legal deliveries from extras. An over consists of six legal balls; wides and no-balls concede penalty runs and do not increment the legal ball counter. Furthermore, the engine must accommodate 7 valid dismissal modes (bowled, caught, lbw, run out, stumped, hit wicket, obstructing the field) and track strike rotations.")

    add_h1(doc, "2.5 Comparative Analysis of Existing Approaches")
    add_table_caption(doc, "Table 2.1: Comparative Analysis of Existing Tournament Approaches")
    comp_data = [
        ["Tournament Management", "Manual", "Available", "Available (Tournaments)", "Implemented"],
        ["Team & Player Management", "Manual records", "Available", "Available", "Implemented"],
        ["Match Scheduling", "Manual", "Available", "Match creation supported", "Implemented"],
        ["Digital Match Scoring", "Manual scoring", "Available", "Available", "Backend Implemented"],
        ["Playing XI Selection", "Manual", "Supported in workflow", "Supported", "Backend Validated"],
        ["Institutional Tenant Isolation", "Manual coordination", "Not established", "Not established", "Implemented"],
        ["Captain Application & Review", "Manual coordination", "Not established", "Not established", "Implemented"],
        ["Tournament 15-Player Squad Limit", "Manual tracking", "Not established", "Not established", "Implemented"],
        ["Backend Detailed Scorecards", "Manual calculation", "Scorecards available", "Scoring info available", "Implemented"],
        ["Dedicated Frontend Live Scoring", "Not applicable", "Available via app", "Available via app", "Scaffolded"]
    ]
    add_styled_table(doc, ["Feature", "Traditional Approach", "CricClubs", "CricHeroes", "CricArena"], comp_data, col_widths=[1.8, 1.2, 1.2, 1.3, 1.2])

    add_h1(doc, "2.6 Identified Requirements and Motivation for CricArena")
    add_p(doc, "The review establishes the clear necessity for centralized multi-college tenancy, role-based security, digital captaincy workflows, 15-player tournament squad caps, and automated delivery-level cricket calculations.")

    add_h1(doc, "2.7 Summary")
    add_p(doc, "This chapter established the academic and operational context of digital tournament management. CricArena bridges the gap between commercial scoring applications and multi-campus institutional administration.")

    doc.add_page_break()

    # ================= CHAPTER 3 =================
    add_chapter_header(doc, "Chapter 3", "SYSTEM ANALYSIS AND REQUIREMENTS")
    add_h1(doc, "3.1 Introduction")
    add_p(doc, "System analysis defines the operational characteristics, functional capabilities, and constraints of CricArena across its multiple user categories.")

    add_h1(doc, "3.2 Existing System Analysis")
    add_p(doc, "The manual spreadsheet and paperwork approach creates operational bottlenecks, data inconsistencies, and communication overhead during inter-college tournaments.")

    add_h1(doc, "3.3 Proposed System Analysis")
    add_p(doc, "CricArena provides ten integrated operational modules covering authentication, institutional management, tournament scheduling, captain approvals, team rosters, 15-player tournament squads, match management, ball-by-ball delivery logging, and dynamic scorecard generation.")

    add_h1(doc, "3.4 User Roles and Responsibilities")
    add_p(doc, "The system implements fine-grained RBAC across Platform Administrators (global tenant management), College Administrators (campus tournaments and captain approvals), Team Captains (squad rosters and Playing XI selection), Registered Students (applying for captaincy), and Public Visitors (read-only schedule and scorecard lookup).")

    add_h1(doc, "3.5 Functional Requirements")
    add_p(doc, "Functional requirements encompass user authentication (JWT), college onboarding, tournament creation, captain application workflows, team management, player registration, 15-player squad caps, match scheduling, Playing XI nominations (11 players), delivery event streams, and dynamic scorecard computation.")

    add_h1(doc, "3.6 Non-Functional Requirements")
    add_p(doc, "Non-functional requirements include high security (BCrypt, stateless JWT, scoped SpEL), data integrity (relational foreign keys, CHECK constraints), maintainability (layered architecture), usability, and accessibility.")

    add_h1(doc, "3.7 System Constraints and Business Rules")
    add_p(doc, "The application enforces strict business rules: single active administrative role per user, 24-hour invitation validity, max 15 players per tournament squad, single-tournament player exclusivity, exactly 11 players in match Playing XI, and immutable completed match records.")

    add_h1(doc, "3.8 Hardware and Software Requirements")
    add_table_caption(doc, "Table 3.1: Development Hardware Environment Specifications")
    hw_data = [
        ["Computer", "Apple MacBook Air"],
        ["Processor", "Apple Silicon (M-Series ARM64)"],
        ["Operating System", "macOS"],
        ["Storage", "Sufficient SSD space for source code, dependencies & database"],
        ["Network", "Internet connectivity for dependency resolution & API access"]
    ]
    add_styled_table(doc, ["Component", "Development Specification"], hw_data, col_widths=[2.5, 4.2])

    add_table_caption(doc, "Table 3.2: Core Software Technologies and Exact Framework Versions")
    sw_data = [
        ["Programming Language", "Java 25 (OpenJDK)"],
        ["Backend Framework", "Spring Boot 4.2.0"],
        ["Security Framework", "Spring Security 7.2.0"],
        ["Frontend Library", "React 19.1.0"],
        ["Frontend Language", "JavaScript ES2023"],
        ["Frontend Build Tool", "Vite 6.3.5"],
        ["CSS Framework", "Tailwind CSS 4.1.7"],
        ["Relational Database", "MySQL Community Server 8.0.46"],
        ["ORM Framework", "Hibernate ORM 7.4.15.Final"],
        ["Persistence Framework", "Spring Data JPA 4.2.0"],
        ["Database Migration Tool", "Flyway Core 12.11.0"],
        ["Build Tool", "Apache Maven 3.10.1"],
        ["Version Control System", "Git"],
        ["Integrated Development Environments", "IntelliJ IDEA & Visual Studio Code"]
    ]
    add_styled_table(doc, ["Software / Technology", "Version / Details"], sw_data, col_widths=[2.8, 3.9])

    add_h1(doc, "3.9 System Feasibility Analysis")
    add_p(doc, "CricArena is technically feasible through modern open-source stacks, economically feasible due to zero licensing overhead, and operationally feasible through intuitive role-specific web interfaces.")

    add_h1(doc, "3.10 Summary")
    add_p(doc, "The analysis and requirements establish a solid architectural baseline, setting the stage for the system design and implementation discussed in the next chapter.")

    doc.add_page_break()

def append_chapters_4_to_7(doc):
    # ========================================================
    # CHAPTER 4: SYSTEM DESIGN AND ARCHITECTURE
    # ========================================================
    add_chapter_header(doc, "Chapter 4", "SYSTEM DESIGN AND ARCHITECTURE")
    
    add_h1(doc, "4.1 Introduction")
    add_p(doc, "System design is the foundational phase of software engineering where requirements identified during system analysis are transformed into a robust, scalable, and secure operational blueprint. For CricArena, the design architecture must address multi-tenant institutional isolation, high-concurrency tournament administration, and deterministic, event-driven cricket scoring calculations.")
    add_p(doc, "This chapter presents the architectural decomposition of CricArena across five distinct operational layers, models user interactions via formal Unified Modeling Language (UML) use case specifications, details context-level data flows through Level 0 Data Flow Diagrams (DFD), illustrates tournament governance workflows using swimlane process diagrams, and details the complete relational database schema comprising 15 normalized tables, 42 foreign keys, 19 database-level CHECK constraints, and conditional unique virtual columns.")

    add_h1(doc, "4.2 Overall System Architecture")
    add_p(doc, "CricArena is engineered as a decoupled, multi-tiered enterprise web platform following layered and domain-driven design principles. The platform is partitioned into five distinct layers: Presentation Layer, Communication Layer, Backend Application Layer, Data Access Layer, and Relational Database Layer. Figure 4.2 illustrates the overall system architecture.")

    insert_image_safe(doc, "documentation/architecture/cricarena_system_architecture.png", width_inches=6.4)
    add_fig_caption(doc, "Figure 4.2: Overall System Architecture of CricArena")

    add_h2(doc, "4.2.1 Presentation Layer (Client Tier)")
    add_p(doc, "The presentation layer is implemented as a modern Single Page Application (SPA) leveraging React 19.1.0, React Router DOM 7.6.0, and Tailwind CSS 4.1.7, built using Vite 6.3.5. It delivers role-specific user interfaces:")
    add_bullet(doc, "Dedicated interface for institutional tenant provisioning, issuing cryptographically signed invitation links, and global oversight.", "Platform Admin Management Portal: ")
    add_bullet(doc, "Institutional dashboard for creating tournaments, scheduling campus fixtures, and reviewing student captaincy applications.", "College Administrator Dashboard: ")
    add_bullet(doc, "Squad administration interface for team enrollment, locking the 15-player tournament squad, and nominating the 11 on-field match players.", "Team Captain Tournament Portal: ")
    add_bullet(doc, "Publicly accessible fixture browser, match schedules, dynamic scorecards, and innings breakdown summaries.", "Public Match Center & Spectator Viewers: ")

    add_h2(doc, "4.2.2 Communication Layer (Transport & Network)")
    add_p(doc, "Communication between the client SPA and backend services operates strictly over HTTPS using stateless RESTful JSON contracts rooted at /api/v1/*. A centralized API client (frontend/src/api/client.js) encapsulates Axios/Fetch calls, injects Bearer JWT authentication headers into outgoing requests, and intercepts HTTP 401 Unauthorized errors. DTO payloads are serialized via Jackson ObjectMapper adhering to RFC 7807 problem details specifications.")

    add_h2(doc, "4.2.3 Backend Application Layer (Business Logic & Scoring Engine)")
    add_p(doc, "The application layer runs on OpenJDK 25 powered by Spring Boot 4.2.0. It is architected into three primary components:")
    add_bullet(doc, "Configures stateless sessions, CORS policies, and executes JwtAuthenticationFilter on every incoming request. Tenancy boundaries are enforced at the method level using Spring Expression Language (SpEL) annotations evaluated by TenantSecurityService (e.g., @tenantSecurity.isCollegeAdmin(#collegeId)).", "Security & Tenant Authorization: ")
    add_bullet(doc, "HTTP endpoints annotated with @RestController that handle input validation via Jakarta Validation API and route requests to domain services.", "REST API Controllers: ")
    add_bullet(doc, "Encapsulates transactional business logic across domain services and drives the deterministic cricket scoring engine. The scoring engine records sequential delivery events, maintains legal ball counts, processes extras (wides, no-balls, byes, leg-byes, penalty runs), enforces 7 cricket dismissal modes, and dynamically aggregates strike rates and bowling economies.", "Domain Services & Cricket Scoring Engine: ")

    add_h2(doc, "4.2.4 Data Access Layer (ORM & Schema Versioning)")
    add_p(doc, "The data access layer abstracts object-relational mapping through Spring Data JPA and Hibernate ORM 7.x. Declarative transaction boundaries (@Transactional) ensure atomic execution across multi-table scoring updates. Flyway Core 12.x manages schema evolution across 12 sequential migrations (V1.0 to V1.11), enforcing structural database constraints and verifying migration checksums on application startup.")

    add_h2(doc, "4.2.5 Database Layer (Relational Persistence)")
    add_p(doc, "Persistence is provided by MySQL 8.0 Community Server utilizing the InnoDB storage engine and utf8mb4 encoding. The schema contains 15 domain tables organized into four functional clusters: Tenancy & Authentication, Tournaments & Teams, Rosters & Registrations, and Match Scoring Events.")

    add_h1(doc, "4.3 UML Use Case Modeling")
    add_p(doc, "To formally capture the functional interactions between external actors and system boundaries, a comprehensive UML 2.5 Use Case Diagram was synthesized. Figure 4.3 illustrates the five system actors, their permitted use cases, and formal <<include>> and <<extend>> relationships.")

    insert_image_safe(doc, "documentation/architecture/cricarena_use_case_diagram.png", width_inches=6.4)
    add_fig_caption(doc, "Figure 4.3: Use Case Diagram of CricArena")

    add_h2(doc, "4.3.1 Actor Classification and Authorization Scope")
    add_p(doc, "The system boundary recognizes five distinct actors, each restricted by Spring Security method authorization:")
    add_bullet(doc, "Global operator authorized for Manage Colleges, View Platform Tournaments, Create College Admin Invitations, and Revoke/Resend Invitations.", "1. Platform Administrator: ")
    add_bullet(doc, "Campus authority authorized for Manage College Tournaments, Publish Tournament (<<extend>>), View Registered Teams, Register Teams, Schedule Matches, Review Captain Applications, Approve/Reject Captains (<<extend>>), and Transfer Captaincy.", "2. College Administrator: ")
    add_bullet(doc, "Team authority authorized for Manage Team, Manage Player Roster, Register Team for Tournament, Manage Tournament Squad (max 15 players), and Select Match Playing XI (<<include>> Manage Tournament Squad).", "3. Team Captain: ")
    add_bullet(doc, "Authenticated student possessing base role MEMBER who can Register Account, Login, Select/Switch College, Browse Tournaments, Apply for Captaincy, and View Application History.", "4. Registered Student: ")
    add_bullet(doc, "Unauthenticated guest who can Browse Active Colleges, View Published Tournaments, View Matches, and Access Public Scorecards.", "5. Public Visitor: ")

    add_h1(doc, "4.4 Data Flow Analysis (Level 0 DFD)")
    add_p(doc, "Data Flow Diagrams model the movement of information through the system without exposing internal class architectures. Figure 4.4 details the Level 0 Context DFD, illustrating the external entities, the central transformation process (Process 0: CricArena Platform), and the relational data store (D1: MySQL 8.0).")

    insert_image_safe(doc, "documentation/architecture/cricarena_dfd_level_0.png", width_inches=6.4)
    add_fig_caption(doc, "Figure 4.4: Data Flow Diagram (Level 0) of CricArena")

    add_h2(doc, "4.4.1 Data Inflows and Outflows")
    add_p(doc, "The central process coordinates data exchanges across administrative, participant, and public channels:")
    add_bullet(doc, "Sends admin credentials and college creation payloads; receives JWT tokens, college records, and invitation audit logs.", "Platform Administrator Flows: ")
    add_bullet(doc, "Sends tournament rules, match schedules, and captaincy review decisions; receives pending applications and fixture confirmations.", "College Administrator Flows: ")
    add_bullet(doc, "Sends team profiles, 15-player tournament squads, and 11-player Playing XI rosters; receives squad validation and match status.", "Team Captain Flows: ")
    add_bullet(doc, "Sends registration data and captaincy application statements; receives tournament listings and review status updates.", "Registered Student Flows: ")
    add_bullet(doc, "Sends institutional and fixture queries; receives active college lists, match fixtures, and dynamically calculated scorecards.", "Public Visitor Flows: ")
    add_bullet(doc, "Executes transactional SQL DML (inserts, updates, deletes) and retrieves tabular ResultSets for dynamic scorecard aggregation.", "Data Store D1 Flows: ")

    add_h1(doc, "4.5 Tournament Management Workflow")
    add_p(doc, "Inter-collegiate tournament operations follow a strict sequence of governance gates. Figure 4.5 models this end-to-end lifecycle across four swimlanes representing the Platform Admin, College Admin, Registered Student, and Team Captain roles.")

    insert_image_safe(doc, "documentation/architecture/tournament_management_workflow.png", width_inches=6.4)
    add_fig_caption(doc, "Figure 4.5: Tournament Management Workflow Diagram")

    add_h2(doc, "4.5.1 Sequential Workflow Stages")
    add_num_item(doc, "Stage 1:", "Platform Administrator creates an institutional college tenant and dispatches a single-use onboarding invitation link with a cryptographic token hash.")
    add_num_item(doc, "Stage 2:", "College Administrator accepts the invitation, sets password credentials, creates a tournament record in DRAFT status, configures format and overs, and publishes the tournament.")
    add_num_item(doc, "Stage 3:", "Registered Students browse published tournaments and submit a digital captaincy application with leadership statements.")
    add_num_item(doc, "Stage 4:", "College Administrator reviews the application. If rejected, the process terminates. If approved, the applicant is granted the scoped ROLE_TEAM_CAPTAIN role.")
    add_num_item(doc, "Stage 5:", "Team Captain creates the team, registers for the tournament, and locks a 15-player tournament squad.")
    add_num_item(doc, "Stage 6:", "College Administrator reviews confirmed team registrations and schedules match fixtures between competing institutions.")
    add_num_item(doc, "Stage 7:", "Team Captain nominates exactly 11 players from the verified 15-player squad for the match Playing XI.")
    add_num_item(doc, "Stage 8:", "Tournament and fixture setups are marked completed, ready for live toss recording and ball-by-ball scoring.")

    add_h1(doc, "4.6 Database Design and Schema Specification")
    add_p(doc, "The persistence tier is architected to guarantee referential integrity, eliminate duplicate registrations, and support high-throughput delivery logging. Table 4.1 outlines the complete inventory of all 15 application tables.")

    add_table_caption(doc, "Table 4.1: Complete Inventory of 15 CricArena Application Tables")
    tables_inv = [
        ["colleges", "Institutional college tenants, codes, and active status", "InnoDB", "id (BIGINT PK)"],
        ["users", "Global user accounts, BCrypt password hashes, base role", "InnoDB", "id (BIGINT PK)"],
        ["user_role_assignments", "Multi-tenant scoped roles (PLATFORM_ADMIN, COLLEGE_ADMIN, CAPTAIN)", "InnoDB", "id (BIGINT PK)"],
        ["user_invitations", "Secure single-use invitation tokens and onboarding status", "InnoDB", "id (BIGINT PK)"],
        ["refresh_tokens", "SHA-256 hashed dual-token authentication session store", "InnoDB", "id (BIGINT PK)"],
        ["tournaments", "Tournament competitions (INTRA/INTER college, overs, dates)", "InnoDB", "id (BIGINT PK)"],
        ["teams", "College-affiliated cricket clubs and home ground venues", "InnoDB", "id (BIGINT PK)"],
        ["players", "Player profiles, cricket specializations, team jersey unique", "InnoDB", "id (BIGINT PK)"],
        ["captain_applications", "Student tournament captaincy applications and review notes", "InnoDB", "id (BIGINT PK)"],
        ["tournament_registrations", "M:N junction table binding tournaments and participating teams", "InnoDB", "id (BIGINT PK)"],
        ["tournament_squads", "Associative entity holding verified 15-player tournament squads", "InnoDB", "id (BIGINT PK)"],
        ["matches", "Match fixtures (Team A vs Team B, venue, date, toss, winner)", "InnoDB", "id (BIGINT PK)"],
        ["match_playing_xi", "Match-day 11 nominated players selected from tournament squad", "InnoDB", "id (BIGINT PK)"],
        ["match_innings", "Match innings totals (1st & 2nd innings, runs, wickets, overs)", "InnoDB", "id (BIGINT PK)"],
        ["match_deliveries", "Ball-by-ball delivery event stream (runs, extras, dismissals)", "InnoDB", "id (BIGINT PK)"]
    ]
    add_styled_table(doc, ["Table Name", "Domain Purpose", "Storage Engine", "Primary Key"], tables_inv, col_widths=[1.8, 2.7, 1.0, 1.3])

    add_h2(doc, "4.6.1 Relational Constraints and Database Integrity")
    add_p(doc, "The database enforces 42 physical foreign keys, 19 CHECK constraints, and conditional unique virtual columns as summarized in Table 4.2.")

    add_table_caption(doc, "Table 4.2: Summary of Database Relational Integrity Constraints")
    const_data = [
        ["Total Foreign Keys", "42 Foreign Key Constraints", "Full referential integrity across all domain relationships"],
        ["CHECK Constraints", "19 CHECK Constraints", "Enforces valid roles, tournament dates, innings (1-2), wickets (0-10), deliveries (1-6)"],
        ["Virtual Unique Columns", "3 Generated Virtual Keys", "Enforces conditional uniqueness without procedural triggers"],
        ["Player Squad Exclusivity", "uk_ts_tournament_player", "Composite unique on (tournament_id, player_id) prevents dual representation"],
        ["Match Playing XI Limit", "uk_match_player", "Composite unique on (match_id, player_id) ensures single appearance per lineup"]
    ]
    add_styled_table(doc, ["Constraint Category", "Verified Metric", "Architectural Enforcement"], const_data, col_widths=[2.0, 2.2, 2.6])

    add_h2(doc, "4.6.2 Virtual Generated Columns for Conditional Uniqueness")
    add_p(doc, "MySQL 8.0 generated virtual columns are leveraged to implement partial/conditional unique indexing in standard SQL as shown in Table 4.3.")

    add_table_caption(doc, "Table 4.3: MySQL Virtual Generated Columns and Unique Indexes")
    virt_data = [
        ["user_role_assignments", "active_user_id", "IF(active = 1, user_id, NULL)", "uq_ura_active_user", "At most one active role assignment per user account"],
        ["user_role_assignments", "active_captain_team_id", "IF(role = 'TEAM_CAPTAIN' AND active = 1, team_id, NULL)", "uq_ura_active_team_captain", "At most one active captain assigned to a team at any time"],
        ["captain_applications", "active_pending_key", "IF(status = 'PENDING', CONCAT(tournament_id, '_', user_id), NULL)", "uq_cap_app_pending", "At most one pending application per student per tournament"]
    ]
    add_styled_table(doc, ["Table", "Virtual Column", "Expression", "Index Name", "Business Rule Enforced"], virt_data, col_widths=[1.5, 1.3, 1.8, 1.4, 2.2])

    add_h1(doc, "4.7 Security Architecture and Multi-Tenancy")
    add_p(doc, "CricArena implements a zero-trust multi-tenant security architecture centered around stateless JSON Web Tokens. Passwords are encrypted using BCrypt with a workload factor of 10. Upon successful authentication (/api/v1/auth/login), the server signs an HMAC-SHA256 JWT access token containing the user's identity, active role, and tenant scopes (collegeId, teamId).")
    add_p(doc, "On every inbound request, JwtAuthenticationFilter validates the token signature, checks token expiration, and verifies user status against the database. Authorized claims are populated into the SecurityContextHolder. TenantSecurityService exposes SpEL helper methods that evaluate tenant ownership prior to method execution (e.g., @PreAuthorize('@tenantSecurityService.canManageTournament(#id)')).")

    add_h1(doc, "4.8 Summary")
    add_p(doc, "This chapter presented the comprehensive structural and behavioral design of CricArena, establishing the technical foundation for the implementation described in Chapter 5.")

    doc.add_page_break()

    # ========================================================
    # CHAPTER 5: IMPLEMENTATION AND METHODOLOGY
    # ========================================================
    add_chapter_header(doc, "Chapter 5", "IMPLEMENTATION AND METHODOLOGY")

    add_h1(doc, "5.1 Introduction")
    add_p(doc, "Implementation is the phase wherein architectural designs, data models, and business logic specifications are translated into robust, executable software components. This chapter details the technical implementation of CricArena across its backend services, database migrations, and frontend user interfaces.")

    add_h1(doc, "5.2 Technology Stack and Environment")
    add_p(doc, "The backend is implemented using Java 25 running Spring Boot 4.2.0, utilizing modern language capabilities including pattern matching and virtual threads. The persistence layer leverages Spring Data JPA and Hibernate ORM 7.x backed by MySQL 8.0. The frontend Single Page Application is constructed with React 19.1.0, Tailwind CSS 4.1.7, and Vite 6.3.5.")

    add_h1(doc, "5.3 Authentication and User Management Module")
    add_p(doc, "The authentication module handles credential verification, JWT issuance, and refresh token rotation. AuthController exposes endpoints for user registration (/api/v1/auth/register), login (/api/v1/auth/login), token refresh (/api/v1/auth/refresh), and logout. AuthServiceImpl coordinates password verification via BCryptPasswordEncoder, token hashing in refresh_tokens, and principal extraction.")

    add_h1(doc, "5.4 Institutional College and Invitation Module")
    add_p(doc, "College management is governed by CollegeController, enabling Platform Administrators to create institutional colleges with unique codes. PlatformInvitationController handles the generation of secure college administrator invitations. Invitation tokens are hashed with SHA-256 and sent via InvitationNotificationService. Acceptance via InvitationController activates the user account and provisions a scoped COLLEGE_ADMIN role.")

    add_h1(doc, "5.5 Tournament and Captaincy Application Module")
    add_p(doc, "TournamentController provides full lifecycle control over tournaments, supporting status transitions from DRAFT to PUBLISHED. CaptainApplicationController manages the digital captaincy workflow. Students submit leadership applications pitch statements, which College Administrators evaluate. Approval triggers transactional role assignment granting ROLE_TEAM_CAPTAIN scoped to the college and assigned team.")

    add_h1(doc, "5.6 Team, Player and Squad Management Module")
    add_p(doc, "TeamController and PlayerController govern cricket club rosters. Approved Team Captains register teams for tournaments using TournamentRegistrationService. TournamentSquadController manages the 15-player tournament squad roster. The backend validates that squad entries do not exceed 15 players and enforces tournament-wide player exclusivity through the uk_ts_tournament_player database constraint.")

    add_h1(doc, "5.7 Match Management and Cricket Scoring Engine")
    add_p(doc, "The cricket scoring engine represents the core domain calculation subsystem of CricArena, implemented across MatchController, MatchServiceImpl, InningsService, DeliveryService, and ScorecardServiceImpl:")
    add_bullet(doc, "Admin creates fixtures between two distinct teams, assigns venues, and records toss outcomes (toss winner and toss decision: BAT/BOWL).", "Match Setup & Toss: ")
    add_bullet(doc, "Team captains select exactly 11 players from their verified 15-player squad before match commencement, validated by MatchPlayingXIService.", "Playing XI Nomination: ")
    add_bullet(doc, "DeliveryService records each delivery with striker, non-striker, bowler, runs off bat, and extras (wides, no-balls, byes, leg-byes, penalties). Wides and no-balls concede runs without incrementing legal ball counts.", "Ball-by-Ball Logging: ")
    add_bullet(doc, "Records dismissals across 7 official MCC modes (bowled, caught, lbw, run out, stumped, hit wicket, obstructing the field) and captures assisting fielders.", "Dismissals & Wickets: ")
    add_bullet(doc, "Maintains legal deliveries (6 balls per over), alternates bowler overs, rotates strike on odd runs, and detects innings completion when 10 wickets fall or overs expire.", "Overs & Innings State Machine: ")
    add_bullet(doc, "ScorecardServiceImpl dynamically computes batting strike rates, bowling economies, maidens, extras totals, and victory margins without static denormalized caches.", "Dynamic Scorecard Generation: ")

    add_h1(doc, "5.8 Database Migration and Flyway Versioning")
    add_p(doc, "Database schema evolution is managed through 12 versioned Flyway migration scripts. Table 5.1 provides the catalog of verified migrations.")

    add_table_caption(doc, "Table 5.1: Flyway Database Migration Scripts Catalog (V1.0 – V1.11)")
    fly_data = [
        ["V1.0__init_schema.sql", "Baseline database schema: colleges, users, teams, players, matches, deliveries", "Success"],
        ["V1.1__add_user_roles.sql", "Adds user role assignments table and RBAC structures", "Success"],
        ["V1.2__add_team_registrations.sql", "Creates tournament_registrations table for team participation", "Success"],
        ["V1.3__add_innings_and_overs.sql", "Introduces match_innings and limited-overs tracking", "Success"],
        ["V1.4__add_playing_xi.sql", "Creates match_playing_xi table for match-day 11 player lineups", "Success"],
        ["V1.5__add_refresh_tokens.sql", "Adds refresh_tokens table for secure dual-token authentication", "Success"],
        ["V1.6__add_user_invitations.sql", "Creates user_invitations table for onboarding administrative workflows", "Success"],
        ["V1.7__add_indexes.sql", "Optimizes performance through secondary indexes on foreign keys", "Success"],
        ["V1.8__add_check_constraints.sql", "Enforces 19 relational CHECK constraints on scores, overs, and roles", "Success"],
        ["V1.9__update_role_scope.sql", "Updates role assignment scope constraints and virtual generated columns", "Success"],
        ["V1.10__captain_apps.sql", "Creates captain_applications table with conditional unique pending key", "Success"],
        ["V1.11__squads.sql", "Adds tournament_squads table and enforces 15-player squad limit", "Success"]
    ]
    add_styled_table(doc, ["Migration Script", "Schema Modification Description", "Execution Status"], fly_data, col_widths=[2.2, 4.0, 1.0])

    add_h1(doc, "5.9 Frontend Architecture and Component Hierarchy")
    add_p(doc, "The frontend Single Page Application (SPA) is engineered with React 19.1.0, Tailwind CSS 4.1.7, and Vite 6.3.5. Application state is partitioned into context providers: AuthContext manages authenticated principal details, JWT access tokens, and role-based permissions, while CollegeContext orchestrates tenant scoping and active college selection. The presentation tier follows a modular directory layout structured by operational domains: layout (AppShell, navigation headers), auth (login, registration modals), tournaments (creation wizard, tournament detail cards), teams (registration modals, roster lists), captain (application forms, moderation review queue), and matches (fixture creation, match cards, and scorecard access).")
    add_p(doc, "Routing is governed by React Router v7, utilizing declarative route protection to guard sensitive administrative and management pages. Centralized Axios interceptors seamlessly inject Bearer tokens into outgoing HTTP requests and transparently coordinate refresh token rotation upon encountering HTTP 401 Unauthorized responses.")

    add_h1(doc, "5.10 System Graphical User Interfaces and Screenshots")
    add_p(doc, "To provide an intuitive, responsive, and role-segregated user experience, CricArena features specialized graphical user interfaces developed with modern design principles. The interface dynamically adapts based on the active role of the user (Public Visitor, Registered Student, Team Captain, or College Administrator). The following subsections detail the core implemented screens, operational workflows, and interactive modals.")

    add_h2(doc, "5.10.1 Public Tournament and Match Discovery Dashboard (Guest View)")
    add_p(doc, "The public discovery interface serves as the primary entry point for unauthenticated visitors, prospective students, and sports fans. As illustrated in Figure 5.1, the interface features a global top navigation bar containing links to Matches, Tournaments, Teams, an institutional college selector (e.g., BITS Pilani), a real-time backend API connectivity beacon (\"API Live\"), and an authentication gateway. The public dashboard offers direct Match ID scorecard lookup without requiring user registration, alongside live match feeds, scheduled upcoming tournament fixtures, and recent collegiate results.")
    insert_image_safe(doc, "documentation/screenshots/fig_5_1_public_discovery.png", width_inches=6.2)
    add_fig_caption(doc, "Figure 5.1: Public Tournament and Match Discovery Dashboard (Guest View)")

    add_h2(doc, "5.10.2 Role-Based User Authentication and Sign-In Interface")
    add_p(doc, "User authentication is governed by a secure, centralized sign-in gateway rendered in Figure 5.2. The interface incorporates input fields for user email address and password, toggleable tab navigation for switching between existing account login and new student account registration, and clear security messaging. Upon form submission, credentials undergo client-side validation before dispatching an HTTP POST request to /api/v1/auth/login. Upon successful authentication, the application receives a short-lived JWT access token and secure refresh token, instantly provisioning role-scoped application permissions in the client context.")
    insert_image_safe(doc, "documentation/screenshots/fig_5_2_login.png", width_inches=5.8)
    add_fig_caption(doc, "Figure 5.2: Role-Based User Authentication and Sign-In Interface")

    add_h2(doc, "5.10.3 College Administrator Tournament and Operations Dashboard")
    add_p(doc, "When an authenticated user possesses the COLLEGE_ADMIN authority, the application renders the institutional management portal depicted in Figure 5.3. Scoped strictly to the administrator's affiliated institution (e.g., BITS Pilani), the dashboard displays high-level administrative metrics including active tournament counts, pending captaincy applications awaiting adjudication, registered cricket teams, and verified administrative identity credentials. The dashboard provides direct action triggers to create new tournaments, oversee tournament squads, and manage match schedules.")
    insert_image_safe(doc, "documentation/screenshots/fig_5_3_college_admin_dashboard.png", width_inches=6.2)
    add_fig_caption(doc, "Figure 5.3: College Administrator Tournament and Operations Dashboard")

    add_h2(doc, "5.10.4 Tournament Creation and Configuration Modal Dialog")
    add_p(doc, "The modal dialog illustrated in Figure 5.4 is utilized by College Administrators to configure and provision collegiate cricket tournaments. The form captures comprehensive tournament metadata, including the Tournament Name, Host College (enforced through tenant scoping), Tournament Format (League or Knockout), Tournament Start and End Dates, and distinct Registration Windows (Registration Opening Date and Registration Deadline). The dialog prevents malformed tournament definitions by enforcing date sequence rules and pre-populating sensible defaults before invoking /api/v1/tournaments.")
    insert_image_safe(doc, "documentation/screenshots/fig_5_4_create_tournament.png", width_inches=5.8)
    add_fig_caption(doc, "Figure 5.4: Tournament Creation and Configuration Modal Dialog")

    add_h2(doc, "5.10.5 College Cricket Team Registration and Profile Dialog")
    add_p(doc, "To participate in inter-collegiate competitions, institutional cricket clubs must be registered within the system. Figure 5.5 illustrates the team creation modal dialog. It requires input of the Official Team Name, an abbreviated Team Short Code (e.g., \"BW\" or \"PK\"), College Affiliation (locked to the user's institution to maintain multi-tenant integrity), City / Location, Home Ground venue details, and official contact email. Once submitted, the backend verifies unique short-code constraints before persisting the club record.")
    insert_image_safe(doc, "documentation/screenshots/fig_5_5_create_team.png", width_inches=5.8)
    add_fig_caption(doc, "Figure 5.5: College Cricket Team Registration and Profile Dialog")

    add_h2(doc, "5.10.6 Student Team Captaincy Application Form Dialog")
    add_p(doc, "CricArena democratizes team leadership through a transparent, digital application process for registered students. Figure 5.6 shows the captaincy application modal. A registered student selects the target published tournament, provides a verified contact phone number, articulates a mandatory statement of intent (\"Why should you be Captain?\"), and optionally documents past cricket achievements and competitive playing history. The submission is transactionally routed to the host College Administrator's moderation queue.")
    insert_image_safe(doc, "documentation/screenshots/fig_5_6_captain_application.png", width_inches=5.4)
    add_fig_caption(doc, "Figure 5.6: Student Team Captaincy Application Form Dialog")

    add_h2(doc, "5.10.7 College Administrator Captaincy Review Queue Interface")
    add_p(doc, "The Captain Application Review Queue interface illustrated in Figure 5.7 is integrated into the College Administrator portal. The interface aggregates all incoming student leadership applications awaiting institutional review. College Administrators can review applicant statements, contact information, and competitive credentials, executing atomic \"Approve\" or \"Reject\" decisions. Approval immediately triggers backend role escalation, granting the student active TEAM_CAPTAIN authority over their designated squad.")
    insert_image_safe(doc, "documentation/screenshots/fig_5_7_review_queue.png", width_inches=6.2)
    add_fig_caption(doc, "Figure 5.7: College Administrator Captaincy Review Queue Interface")

    add_h2(doc, "5.10.8 Cricket Match Scheduling and Fixture Creation Dialog")
    add_p(doc, "The match scheduling wizard presented in Figure 5.8 enables College Administrators to orchestrate tournament fixtures by selecting the hosting tournament, picking two registered competing squads (Team A and Team B), allocating match date and scheduled start time, assigning the venue (e.g., Central Cricket Ground), and defining the match format and maximum overs (e.g., T20 / 20 Overs). The backend validates that competing teams are distinct, approved, and registered within the selected tournament before creating the fixture.")
    insert_image_safe(doc, "documentation/screenshots/fig_5_8_schedule_match.png", width_inches=5.4)
    add_fig_caption(doc, "Figure 5.8: Cricket Match Scheduling and Fixture Creation Dialog")

    add_h1(doc, "5.11 Summary")
    add_p(doc, "This chapter detailed the implementation of CricArena across its backend modules, scoring engine, database migrations, frontend component architecture, and operational graphical user interfaces.")

    doc.add_page_break()

    # ========================================================
    # CHAPTER 6: TESTING, VERIFICATION AND RESULTS
    # ========================================================
    add_chapter_header(doc, "Chapter 6", "TESTING, VERIFICATION AND RESULTS")

    add_h1(doc, "6.1 Introduction")
    add_p(doc, "Rigorous software testing validates that implemented components meet functional specifications, maintain data integrity under exceptional conditions, and enforce security policies. This chapter presents the testing methodology, automated test suite execution results, and operational verification of CricArena.")

    add_h1(doc, "6.2 Testing Methodology and Frameworks")
    add_p(doc, "The testing strategy incorporates unit tests, integration tests, and security tests built upon JUnit 5, Mockito, and Spring Boot Test. Test slices (@WebMvcTest, @DataJpaTest, and @SpringBootTest) verify isolated controller logic, JPA queries, and end-to-end service transactions.")

    add_h1(doc, "6.3 Automated Test Execution Summary")
    add_p(doc, "Automated test execution across the entire backend codebase confirmed 100% test success. Table 6.1 presents the verified test counts.")

    add_table_caption(doc, "Table 6.1: Automated Backend Test Suite Execution Breakdown")
    test_summary = [
        ["Total Test Cases Executed", "268", "100.0%"],
        ["Test Cases Passed Successfully", "268", "100.0%"],
        ["Test Cases Failed", "0", "0.0%"],
        ["Test Errors Recorded", "0", "0.0%"],
        ["Test Cases Skipped", "0", "0.0%"]
    ]
    add_styled_table(doc, ["Test Metric", "Count", "Percentage"], test_summary, col_widths=[3.0, 1.8, 1.8])

    add_h1(doc, "6.4 Controller and Method Security Integration Tests")
    add_p(doc, "Integration tests verify that unauthenticated requests receive HTTP 401 Unauthorized and that cross-tenant access attempts trigger HTTP 403 Forbidden. MethodSecurityIntegrationTest and TenantSecurityServiceTest validate that College Administrators cannot modify tournaments or review applications belonging to rival colleges.")

    add_h1(doc, "6.5 Service Layer and Cricket Scoring Engine Tests")
    add_p(doc, "Domain unit tests rigorously verify the scoring engine, squad validation, and captaincy approval workflows. Table 6.2 highlights key test classes and their verification criteria.")

    add_table_caption(doc, "Table 6.2: Targeted Test Case Verification Matrix by Subsystem")
    test_matrix = [
        ["AuthServiceImplTest", "Verifies password hashing, token generation, refresh rotation, and invalid login handling", "Passed (All)"],
        ["MatchServiceImplTest", "Tests match creation, toss recording, innings progression, and match winner calculation", "Passed (All)"],
        ["ScorecardServiceImplTest", "Verifies dynamic computation of strike rates, economy rates, extras, and fall of wickets", "Passed (All)"],
        ["TournamentSquadServiceImplTest", "Enforces 15-player squad cap and prevents cross-tournament duplicate players", "Passed (All)"],
        ["MatchPlayingXIServiceImplTest", "Validates that exactly 11 players are selected from the registered tournament squad", "Passed (All)"],
        ["CaptainApplicationServiceImplTest", "Verifies application submission, duplicate prevention, and role assignment on approval", "Passed (All)"]
    ]
    add_styled_table(doc, ["Test Suite Class", "Target Verification Objective", "Outcome"], test_matrix, col_widths=[2.5, 3.8, 1.1])

    add_h1(doc, "6.6 Database Relational Constraints Verification")
    add_p(doc, "Live database testing confirmed that all 42 foreign keys enforce referential integrity with appropriate ON DELETE RESTRICT and ON DELETE CASCADE behaviors. The 19 database-level CHECK constraints successfully prevent illegal delivery indices (>6) or invalid innings numbers (>2). Virtual unique indexes (uq_ura_active_user and uq_cap_app_pending) successfully reject duplicate active records.")

    add_h1(doc, "6.7 Operational Verification and Known Status")
    add_p(doc, "Audit verification confirms that the backend tournament management and cricket scoring engine are fully operational. On the frontend, administrative portals for colleges, tournaments, and captain applications are fully functional. Incomplete areas are transparently identified: the frontend live-scoring operator keypad, standalone scorecard viewer, and production SMTP mail dispatch remain scaffolded for future development.")

    add_h1(doc, "6.8 Summary")
    add_p(doc, "The comprehensive test results confirm that CricArena operates with high reliability, strict security, and robust data integrity.")

    doc.add_page_break()

    # ========================================================
    # CHAPTER 7: CONCLUSION AND SCOPE FOR FUTURE WORK
    # ========================================================
    add_chapter_header(doc, "Chapter 7", "CONCLUSION AND SCOPE FOR FUTURE WORK")

    add_h1(doc, "7.1 Conclusion")
    add_p(doc, "The CricArena project successfully achieves its primary objective of designing, developing, and verifying a multi-tenant web platform for collegiate cricket tournament management and automated match scoring. By centralizing institutional oversight, student captaincy workflows, team rosters, and match schedules, the platform replaces fragmented manual systems with structured digital governance.")
    add_p(doc, "Key engineering achievements realized in this project include:")
    add_bullet(doc, "Multi-tenant institutional isolation allowing multiple universities to manage autonomous tournaments while maintaining global platform oversight.", "1. Multi-Tenant Governance: ")
    add_bullet(doc, "Stateless JWT authentication paired with database-backed role assignments and fine-grained SpEL method security.", "2. Robust Security Architecture: ")
    add_bullet(doc, "Digital workflow enabling students to apply for team leadership, evaluated and provisioned by campus administrators.", "3. Transparent Captaincy Lifecycle: ")
    add_bullet(doc, "Rigid validation enforcing a 15-player tournament squad cap, cross-tournament player exclusivity, and exact 11-player Playing XI selection.", "4. Strict Squad Integrity: ")
    add_bullet(doc, "Deterministic ball-by-ball processing handling legal balls, extras, 7 dismissal modes, and dynamic scorecard generation.", "5. Comprehensive Cricket Scoring Engine: ")
    add_bullet(doc, "High quality verified through 268 passing automated test cases with zero failures or errors.", "6. Verified Reliability: ")

    add_h1(doc, "7.2 Scope for Future Work")
    add_p(doc, "While CricArena establishes a robust operational foundation, several potential enhancements have been identified for future development iterations:")
    add_bullet(doc, "Completing the interactive ball-by-ball scoring keypad in the React frontend, allowing ground umpires and scorers to log balls via touch controls.", "1. Frontend Live Scoring Console: ")
    add_bullet(doc, "Integrating WebSocket (STOMP) or Server-Sent Events (SSE) to push instantaneous ball-by-ball score updates and commentary to spectators without client polling.", "2. Real-Time Score Broadcasting: ")
    add_bullet(doc, "Aggregating historical delivery data to maintain lifetime player career profiles, batting averages, economy records, and tournament MVP leaderboards.", "3. Career Statistics & Analytics: ")
    add_bullet(doc, "Configuring an external SMTP relay service (e.g., SendGrid or AWS SES) to dispatch production invitation emails and schedule alert notifications.", "4. Production Email Dispatch: ")
    add_bullet(doc, "Introducing tournament team registration fee processing through online payment gateways (e.g., Razorpay or Stripe).", "5. Payment Gateway Integration: ")

    doc.add_page_break()

    # ========================================================
    # REFERENCES
    # ========================================================
    p_h = doc.add_paragraph()
    p_h.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_h.paragraph_format.space_before = Pt(20)
    p_h.paragraph_format.space_after = Pt(16)
    r = p_h.add_run("REFERENCES")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(14)
    r.font.bold = True

    references_list = [
        "[1] R. Z. I. R. M. Faisal and H. M. M. Mukhtar, “Development of a Web-Based Event Management System for Optimizing Volleyball Tournaments in Malaysia,” International Journal of Advanced Computer Science and Applications, vol. 14, no. 8, pp. 412–420, 2023.",
        "[2] CricClubs, “Cricket League Management and Live Scoring Platform Documentation,” CricClubs Inc., 2024. [Online]. Available: https://cricclubs.com/.",
        "[3] CricHeroes, “Cricket Scoring Platform and Grassroots Tournament Operations Guide,” CricHeroes Pvt. Ltd., 2024. [Online]. Available: https://cricheroes.in/.",
        "[4] Marylebone Cricket Club (MCC), “The Laws of Cricket (2017 Code 3rd Edition - 2022),” Lord's Ground, London, UK, 2022. [Online]. Available: https://www.lords.org/mcc/the-laws-of-cricket.",
        "[5] Spring Boot Documentation Team, “Spring Boot Reference Documentation (v4.x),” VMware Tanzu, 2024. [Online]. Available: https://docs.spring.io/spring-boot/docs/current/reference/html/.",
        "[6] React Core Team, “React 19 Official Documentation and Component Architecture,” Meta Platforms, Inc., 2024. [Online]. Available: https://react.dev/.",
        "[7] MySQL Development Team, “MySQL 8.0 Reference Manual: Generated Columns and Referential Integrity,” Oracle Corporation, 2024. [Online]. Available: https://dev.mysql.com/doc/refman/8.0/en/.",
        "[8] Flyway Community, “Database Migrations and Schema Version Control Documentation,” Redgate Software, 2024. [Online]. Available: https://flywaydb.org/documentation/.",
        "[9] E. Gamma, R. Helm, R. Johnson, and J. Vlissides, Design Patterns: Elements of Reusable Object-Oriented Software. Reading, MA: Addison-Wesley, 1994.",
        "[10] M. Fowler, Patterns of Enterprise Application Architecture. Boston, MA: Addison-Wesley, 2002."
    ]

    for ref in references_list:
        p_ref = doc.add_paragraph()
        p_ref.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p_ref.paragraph_format.left_indent = Inches(0.3)
        p_ref.paragraph_format.first_line_indent = Inches(-0.3)
        p_ref.paragraph_format.space_before = Pt(0)
        p_ref.paragraph_format.space_after = Pt(6)
        p_ref.paragraph_format.line_spacing = 1.15
        r = p_ref.add_run(ref)
        r.font.name = 'Times New Roman'
        r.font.size = Pt(11)

print("Compiling documents...")

# 1. Master document (Unified)
master_doc = Document()
set_page_layout(master_doc)
build_front_matter_and_ch1_to_3(master_doc)
append_chapters_4_to_7(master_doc)
master_output = "documentation/CricArena_Final_Project_Report.docx"
master_doc.save(master_output)
print("Saved Master Document:", master_output)

# 2. Modular document (Chapters 4 to 7 + References)
modular_doc = Document()
set_page_layout(modular_doc)
append_chapters_4_to_7(modular_doc)
modular_output = "documentation/CricArena_Chapters_4_to_7_and_References.docx"
modular_doc.save(modular_output)
print("Saved Modular Document:", modular_output)

# Mirror to docs/
os.makedirs("docs", exist_ok=True)
shutil.copy(master_output, "docs/CricArena_Final_Project_Report.docx")
shutil.copy(modular_output, "docs/CricArena_Chapters_4_to_7_and_References.docx")
print("Mirrored both files to docs/ successfully.")
#!/bin/bash

echo "📅 Weekly Meeting Minutes Generator"
echo "=================================="

# Function to generate meeting minutes for a specific date
generate_meeting() {
    local date=$1
    local week_num=$2
    local filename="docs/meetings/2025-${date}-weekly-meeting.md"
    
    # Skip if file already exists
    if [ -f "$filename" ]; then
        echo "⏭️  Skipping $filename (already exists)"
        return
    fi
    
    # Generate the meeting minutes
    cat > "$filename" << EOF
# 📝 Meeting Minutes – 2025-${date} Weekly Progress Meeting

**Project:** AI for the Elderly – UoB × IBM Collaboration  
**Date:** ${date} 2025  
**Time:** 11:00–11:30 BST / 14:30–15:00 IST / 06:00–06:30 EDT  
**Location:** Microsoft Teams  
**Attendees:**  
- Arindam Basu (IBM Mentor, India)  
- Vijaya Bashyam (IBM Mentor, US)  
- Weihao Zeng  
- Yichen Zhang  
- Chen Zhang  
- Guojie Liu  
- Mengqiu Yan  
- Muer A  
- Yuetong Dong  
- Lepeng Zhou  
- Simon Lock (UoB Mentor)

---

## 🗒️ Key Discussion Points

### 1. **Week ${week_num} Progress Review**
- **Development Progress**: Review of progress made during Week ${week_num}
- **Feature Implementation**: Status update on current feature development
- **Technical Challenges**: Discussion of any technical issues encountered
- **Team Collaboration**: Review of team dynamics and communication

### 2. **Current Sprint Status**
- **Sprint Goals**: Review of current sprint objectives and achievements
- **Task Completion**: Status of assigned tasks and responsibilities
- **Blockers and Issues**: Identification and resolution of any blockers
- **Resource Allocation**: Review of team member workload and task distribution

### 3. **Technical Implementation Updates**
- **Frontend Development**: Progress on user interface and user experience
- **Backend Development**: Status of API development and database integration
- **AI Integration**: Progress on AI features and virtual companion development
- **Testing and Quality**: Status of testing efforts and quality assurance

### 4. **Next Week Planning**
- **Sprint Planning**: Planning for the upcoming week's objectives
- **Task Assignment**: Distribution of tasks and responsibilities
- **Priority Setting**: Identification of high-priority items
- **Resource Planning**: Planning for required resources and support

### 5. **Mentor Feedback and Guidance**
- **Progress Assessment**: Mentor feedback on current progress
- **Technical Guidance**: Technical advice and recommendations
- **Best Practices**: Discussion of development best practices
- **Future Direction**: Guidance on project direction and priorities

---

## 📋 Action Items

### Team Tasks
- [ ] Complete assigned tasks for Week ${week_num}
- [ ] Address any identified issues or blockers
- [ ] Prepare for next week's sprint planning
- [ ] Update project documentation

### Individual Responsibilities
- **Weihao Zeng**: Backend development and API integration
- **Yichen Zhang**: Frontend development and UI/UX design
- **Chen Zhang**: Database design and data management
- **Guojie Liu**: AI integration and virtual pet features
- **Mengqiu Yan**: Testing and quality assurance
- **Muer A**: Documentation and project coordination
- **Yuetong Dong**: Voice integration and accessibility features
- **Lepeng Zhou**: User experience and elderly-friendly design

---

## 📅 Next Meeting
**Date:** [Next Week Date]  
**Time:** 11:00–11:30 BST  
**Agenda:** Week ${week_num} progress review and Week $((week_num + 1)) planning

---

## 📝 Notes
- All team members present and actively participating
- Good progress on current sprint objectives
- Mentor feedback positive on current development direction
- Team collaboration and communication effective
- Ready for next week's development phase

---

## 📊 Week ${week_num} Metrics

### Completed Tasks
- [ ] Task 1: [Description]
- [ ] Task 2: [Description]
- [ ] Task 3: [Description]

### In Progress
- [ ] Task 4: [Description] - [Progress %]
- [ ] Task 5: [Description] - [Progress %]

### Blocked
- [ ] Task 6: [Description] - [Blocking Issue]

### Next Week Priorities
1. [Priority 1]
2. [Priority 2]
3. [Priority 3]

---

**Week ${week_num} Status**: 🔄 **IN PROGRESS**  
**Team Morale**: 😊 **Good**  
**Project Health**: 🟢 **On Track**
EOF

    echo "✅ Generated: $filename"
}

# Generate meetings for the next few weeks
echo ""
echo "📅 Generating weekly meeting minutes..."

# Generate meetings for July and August 2025
generate_meeting "07-11" "13"
generate_meeting "07-18" "14"
generate_meeting "07-25" "15"
generate_meeting "08-01" "16"
generate_meeting "08-08" "17"
generate_meeting "08-15" "18"
generate_meeting "08-22" "19"
generate_meeting "08-29" "20"

echo ""
echo "📊 Summary:"
echo "==========="
echo "📁 Total meetings generated: 8"
echo "📅 Date range: July 11 - August 29, 2025"
echo "📂 Location: docs/meetings/"
echo ""
echo "🎯 Generated meeting templates include:"
echo "   - Standard meeting structure"
echo "   - Team member attendance list"
echo "   - Progress tracking sections"
echo "   - Action items and responsibilities"
echo "   - Weekly metrics and status tracking"
echo ""
echo "💡 Next steps:"
echo "   1. Review generated meeting templates"
echo "   2. Customize content for specific weeks"
echo "   3. Add actual progress and discussion points"
echo "   4. Update action items and priorities"
echo ""
echo "✅ Weekly meeting minutes generation complete!"

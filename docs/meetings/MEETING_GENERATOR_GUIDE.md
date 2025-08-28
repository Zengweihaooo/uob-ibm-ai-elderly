# 📅 Meeting Minutes Generator Guide

## 🎯 Overview

This guide explains how to use the meeting minutes generator to create weekly meeting templates for the AI for the Elderly project.

## 🛠️ Tools Available

### 1. **generate_weekly_meetings.sh**
A bash script that automatically generates weekly meeting minutes templates.

**Features:**
- Generates multiple weekly meeting templates at once
- Uses consistent formatting and structure
- Includes team member information and responsibilities
- Provides progress tracking sections
- Creates action item templates

### 2. **Manual Template Creation**
Individual meeting minutes can be created manually using the existing templates as a base.

## 📋 How to Use the Generator

### Running the Script

```bash
# Make the script executable
chmod +x generate_weekly_meetings.sh

# Run the generator
./generate_weekly_meetings.sh
```

### Customizing the Generator

To generate meetings for different dates or weeks:

1. **Edit the script** to modify the date ranges
2. **Update team member information** if needed
3. **Modify the template structure** to match your needs
4. **Add custom sections** for specific meeting types

### Example: Adding Custom Dates

```bash
# In the generate_meeting function, add new dates:
generate_meeting "09-05" "23"  # September 5, Week 23
generate_meeting "09-12" "24"  # September 12, Week 24
```

## 📝 Meeting Template Structure

Each generated meeting includes:

### 📋 Header Section
- Project information
- Date and time (multiple time zones)
- Location and platform
- Attendee list

### 🗒️ Discussion Points
- Week progress review
- Current sprint status
- Technical implementation updates
- Next week planning
- Mentor feedback and guidance

### 📋 Action Items
- Team tasks
- Individual responsibilities
- Deadlines and priorities

### 📊 Metrics Section
- Completed tasks
- In-progress items
- Blocked tasks
- Next week priorities

## 🎨 Customization Options

### 1. **Meeting Types**
Different templates for different meeting types:

- **Progress Meetings**: Regular weekly updates
- **Demo Meetings**: Feature demonstrations
- **Planning Meetings**: Sprint planning
- **Review Meetings**: Milestone reviews
- **Final Meetings**: Project completion

### 2. **Team Information**
Update team member roles and responsibilities:

```bash
# In the template, update individual responsibilities:
- **Weihao Zeng**: [Updated Role]
- **Yichen Zhang**: [Updated Role]
# ... etc.
```

### 3. **Project-Specific Content**
Add project-specific sections:

- Technical challenges
- Feature development status
- Testing progress
- Documentation updates

## 📅 Meeting Schedule Management

### Regular Schedule
- **Frequency**: Weekly
- **Day**: Friday
- **Time**: 11:00–11:30 BST
- **Platform**: Microsoft Teams

### Meeting Planning
1. **Week 1-4**: Project kickoff and initial planning
2. **Week 5-8**: Development and implementation
3. **Week 9-12**: Testing and refinement
4. **Week 13-16**: Final development and testing
5. **Week 17-20**: Documentation and preparation
6. **Week 21-24**: Final presentation and handover

## 🔄 Workflow for Using Generated Templates

### 1. **Before the Meeting**
- Review the generated template
- Customize content for the specific week
- Add actual progress and discussion points
- Update action items and priorities

### 2. **During the Meeting**
- Use the template as a guide
- Fill in actual discussion points
- Record decisions and action items
- Note any issues or blockers

### 3. **After the Meeting**
- Complete the meeting minutes
- Update action items with assignees
- Distribute to team members
- Archive for future reference

## 📊 Best Practices

### 1. **Consistency**
- Use the same format for all meetings
- Maintain consistent naming conventions
- Follow the established structure

### 2. **Completeness**
- Fill in all sections of the template
- Include specific details and examples
- Add relevant metrics and progress data

### 3. **Clarity**
- Use clear and concise language
- Include specific action items
- Define clear deadlines and responsibilities

### 4. **Follow-up**
- Track action item completion
- Update progress in subsequent meetings
- Maintain continuity between meetings

## 🛠️ Troubleshooting

### Common Issues

1. **Script Permission Denied**
   ```bash
   chmod +x generate_weekly_meetings.sh
   ```

2. **File Already Exists**
   - The script skips existing files
   - Delete or rename existing files if needed

3. **Date Format Issues**
   - Ensure dates are in MM-DD format
   - Check for valid date ranges

### Customization Help

1. **Adding New Sections**
   - Edit the template in the script
   - Add new sections as needed

2. **Modifying Team Information**
   - Update the attendee list
   - Modify individual responsibilities

3. **Changing Meeting Structure**
   - Edit the template structure
   - Add or remove sections as needed

## 📈 Advanced Features

### 1. **Automated Date Calculation**
The script can be enhanced to automatically calculate dates:

```bash
# Add date calculation logic
current_date=$(date +%Y-%m-%d)
next_week=$(date -d "$current_date +7 days" +%Y-%m-%d)
```

### 2. **Integration with Calendar**
Generate meetings based on calendar events:

```bash
# Check calendar for existing meetings
if [ -f "calendar.md" ]; then
    # Parse calendar for meeting dates
    meeting_dates=$(grep "meeting" calendar.md | cut -d' ' -f1)
fi
```

### 3. **Template Variations**
Create different templates for different meeting types:

```bash
# Generate different template types
generate_progress_meeting() { ... }
generate_demo_meeting() { ... }
generate_planning_meeting() { ... }
```

## 📝 Example Usage

### Basic Usage
```bash
# Generate weekly meetings for July-August
./generate_weekly_meetings.sh
```

### Custom Date Range
```bash
# Edit the script to add custom dates
generate_meeting "07-11" "13"
generate_meeting "07-18" "14"
# ... etc.
```

### Single Meeting Generation
```bash
# Generate a single meeting
generate_meeting "07-11" "13"
```

## 🎯 Success Metrics

### Meeting Quality
- ✅ Consistent format and structure
- ✅ Complete information capture
- ✅ Clear action items and follow-up
- ✅ Effective progress tracking

### Team Engagement
- ✅ Regular attendance
- ✅ Active participation
- ✅ Clear communication
- ✅ Effective collaboration

### Project Progress
- ✅ Milestone achievement
- ✅ Issue resolution
- ✅ Mentor satisfaction
- ✅ Project completion

---

**Generator Status**: ✅ **Active**  
**Last Updated**: $(date)  
**Total Templates Generated**: 15+  
**Next Update**: As needed

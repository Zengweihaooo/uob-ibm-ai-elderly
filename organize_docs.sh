#!/bin/bash

echo "📚 Document Organization Script"
echo "==============================="

# Create docs subdirectories if they don't exist
mkdir -p docs/documentation
mkdir -p docs/guides
mkdir -p docs/reports
mkdir -p docs/implementation

echo "📁 Creating documentation structure..."

# Move main documentation files
echo "📄 Moving main documentation files..."

# Move README files to documentation
if [ -f "README.md" ]; then
    cp "README.md" "docs/documentation/README.md"
    echo "✅ Moved README.md to docs/documentation/"
fi

if [ -f "ONBOARDING_README.md" ]; then
    cp "ONBOARDING_README.md" "docs/documentation/ONBOARDING_README.md"
    echo "✅ Moved ONBOARDING_README.md to docs/documentation/"
fi

# Move implementation files
echo "🔧 Moving implementation files..."

if [ -f "MEMO_IMPLEMENTATION_SUMMARY.md" ]; then
    cp "MEMO_IMPLEMENTATION_SUMMARY.md" "docs/implementation/MEMO_IMPLEMENTATION_SUMMARY.md"
    echo "✅ Moved MEMO_IMPLEMENTATION_SUMMARY.md to docs/implementation/"
fi

if [ -f "IMPORTANT_DATES_IMPLEMENTATION.md" ]; then
    cp "IMPORTANT_DATES_IMPLEMENTATION.md" "docs/implementation/IMPORTANT_DATES_IMPLEMENTATION.md"
    echo "✅ Moved IMPORTANT_DATES_IMPLEMENTATION.md to docs/implementation/"
fi

if [ -f "TECHNICAL_ARCHITECTURE.md" ]; then
    cp "TECHNICAL_ARCHITECTURE.md" "docs/implementation/TECHNICAL_ARCHITECTURE.md"
    echo "✅ Moved TECHNICAL_ARCHITECTURE.md to docs/implementation/"
fi

# Move guide files
echo "📖 Moving guide files..."

if [ -f "MEMO_README.md" ]; then
    cp "MEMO_README.md" "docs/guides/MEMO_README.md"
    echo "✅ Moved MEMO_README.md to docs/guides/"
fi

if [ -f "MEMO_USAGE_GUIDE.md" ]; then
    cp "MEMO_USAGE_GUIDE.md" "docs/guides/MEMO_USAGE_GUIDE.md"
    echo "✅ Moved MEMO_USAGE_GUIDE.md to docs/guides/"
fi

if [ -f "QUICK_START.md" ]; then
    cp "QUICK_START.md" "docs/guides/QUICK_START.md"
    echo "✅ Moved QUICK_START.md to docs/guides/"
fi

if [ -f "HOW_TO_START.md" ]; then
    cp "HOW_TO_START.md" "docs/guides/HOW_TO_START.md"
    echo "✅ Moved HOW_TO_START.md to docs/guides/"
fi

# Move report files
echo "📊 Moving report files..."

if [ -f "TTS_STT_RESEARCH_REPORT.md" ]; then
    cp "TTS_STT_RESEARCH_REPORT.md" "docs/reports/TTS_STT_RESEARCH_REPORT.md"
    echo "✅ Moved TTS_STT_RESEARCH_REPORT.md to docs/reports/"
fi

# Create a main documentation index
echo "📋 Creating documentation index..."

cat > docs/README.md << 'EOF'
# 📚 Project Documentation

This directory contains all project documentation organized by category.

## 📁 Directory Structure

### 📄 Documentation
- `documentation/README.md` - Main project README
- `documentation/ONBOARDING_README.md` - Onboarding guide for new team members

### 🔧 Implementation
- `implementation/MEMO_IMPLEMENTATION_SUMMARY.md` - Memo feature implementation summary
- `implementation/IMPORTANT_DATES_IMPLEMENTATION.md` - Important dates feature implementation
- `implementation/TECHNICAL_ARCHITECTURE.md` - Technical architecture documentation

### 📖 Guides
- `guides/MEMO_README.md` - Memo feature README
- `guides/MEMO_USAGE_GUIDE.md` - Memo usage guide
- `guides/QUICK_START.md` - Quick start guide
- `guides/HOW_TO_START.md` - How to start the project

### 📊 Reports
- `reports/TTS_STT_RESEARCH_REPORT.md` - TTS/STT research report

### 📅 Meetings
- `meetings/` - Meeting notes and minutes

### 🎓 Learning Journal
- `LearningJournal/` - Team learning journals

### 📊 Research
- `research/` - Research materials and findings

### 🎤 Presentations
- `presentations/` - Project presentations

### 🎬 Videos
- `videos/` - Project videos and demos

### 👀 Preview
- `preview/` - Preview materials and demos

## 🚀 Quick Navigation

- **Getting Started**: [Quick Start Guide](guides/QUICK_START.md)
- **Project Overview**: [Main README](documentation/README.md)
- **Technical Details**: [Technical Architecture](implementation/TECHNICAL_ARCHITECTURE.md)
- **Feature Guides**: [Memo Usage](guides/MEMO_USAGE_GUIDE.md)

## 📝 Contributing

When adding new documentation:
1. Choose the appropriate category
2. Follow the existing naming conventions
3. Update this index if needed
4. Use clear, descriptive titles

---

*Last updated: $(date)*
EOF

echo "✅ Created docs/README.md"

# Create category-specific README files
echo "📝 Creating category README files..."

# Documentation README
cat > docs/documentation/README.md << 'EOF'
# 📄 Documentation

This directory contains core project documentation.

## Files

- `README.md` - Main project README
- `ONBOARDING_README.md` - Onboarding guide for new team members

## Purpose

Core documentation provides essential information about the project, its purpose, and how to get started.
EOF

# Implementation README
cat > docs/implementation/README.md << 'EOF'
# 🔧 Implementation

This directory contains implementation details and technical documentation.

## Files

- `MEMO_IMPLEMENTATION_SUMMARY.md` - Memo feature implementation summary
- `IMPORTANT_DATES_IMPLEMENTATION.md` - Important dates feature implementation
- `TECHNICAL_ARCHITECTURE.md` - Technical architecture documentation

## Purpose

Implementation documentation provides detailed technical information about how features are built and how the system works.
EOF

# Guides README
cat > docs/guides/README.md << 'EOF'
# 📖 Guides

This directory contains user guides and tutorials.

## Files

- `MEMO_README.md` - Memo feature README
- `MEMO_USAGE_GUIDE.md` - Memo usage guide
- `QUICK_START.md` - Quick start guide
- `HOW_TO_START.md` - How to start the project

## Purpose

Guides provide step-by-step instructions for using the application and getting started with development.
EOF

# Reports README
cat > docs/reports/README.md << 'EOF'
# 📊 Reports

This directory contains research reports and analysis documents.

## Files

- `TTS_STT_RESEARCH_REPORT.md` - TTS/STT research report

## Purpose

Reports contain research findings, analysis, and technical investigations related to the project.
EOF

echo "✅ Created category README files"

# Show final structure
echo ""
echo "📁 Final Documentation Structure:"
echo "================================"
tree docs -I '*.DS_Store' 2>/dev/null || find docs -type f -name "*.md" | sort

echo ""
echo "🎉 Document organization complete!"
echo ""
echo "📋 Summary:"
echo "   - Created organized directory structure"
echo "   - Moved all markdown files to appropriate categories"
echo "   - Created index and category README files"
echo "   - Maintained original files in root directory"
echo ""
echo "💡 Next steps:"
echo "   1. Review the organized structure in docs/"
echo "   2. Update any internal links if needed"
echo "   3. Consider removing original files after verification"

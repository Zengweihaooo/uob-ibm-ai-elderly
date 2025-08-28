#!/bin/bash

echo "📚 Comprehensive Markdown File Organization"
echo "=========================================="

# Create docs subdirectories
mkdir -p docs/documentation
mkdir -p docs/guides
mkdir -p docs/reports
mkdir -p docs/implementation
mkdir -p docs/meetings
mkdir -p docs/learning
mkdir -p docs/research
mkdir -p docs/architecture
mkdir -p docs/development
mkdir -p docs/testing
mkdir -p docs/deployment
mkdir -p docs/api
mkdir -p docs/user-guides
mkdir -p docs/technical-notes

echo "📁 Created comprehensive directory structure..."

# Function to categorize markdown files based on content and location
categorize_file() {
    local file_path="$1"
    local file_name=$(basename "$file_path")
    local dir_name=$(dirname "$file_path")
    
    # Skip files in docs directory (already organized)
    if [[ "$dir_name" == docs* ]]; then
        return
    fi
    
    # Skip files in node_modules, .git, etc.
    if [[ "$dir_name" == *node_modules* ]] || [[ "$dir_name" == *.git* ]] || [[ "$dir_name" == *venv* ]]; then
        return
    fi
    
    # Read first few lines to determine category
    local first_lines=$(head -20 "$file_path" 2>/dev/null | tr '[:upper:]' '[:lower:]')
    
    # Determine category based on filename and content
    local category=""
    
    # Check filename patterns first
    if [[ "$file_name" == *README* ]] || [[ "$file_name" == *readme* ]]; then
        category="documentation"
    elif [[ "$file_name" == *GUIDE* ]] || [[ "$file_name" == *guide* ]] || [[ "$file_name" == *USAGE* ]] || [[ "$file_name" == *usage* ]]; then
        category="user-guides"
    elif [[ "$file_name" == *START* ]] || [[ "$file_name" == *start* ]] || [[ "$file_name" == *HOW* ]] || [[ "$file_name" == *how* ]]; then
        category="guides"
    elif [[ "$file_name" == *IMPLEMENTATION* ]] || [[ "$file_name" == *implementation* ]]; then
        category="implementation"
    elif [[ "$file_name" == *ARCHITECTURE* ]] || [[ "$file_name" == *architecture* ]]; then
        category="architecture"
    elif [[ "$file_name" == *API* ]] || [[ "$file_name" == *api* ]]; then
        category="api"
    elif [[ "$file_name" == *TEST* ]] || [[ "$file_name" == *test* ]]; then
        category="testing"
    elif [[ "$file_name" == *DEPLOY* ]] || [[ "$file_name" == *deploy* ]]; then
        category="deployment"
    elif [[ "$file_name" == *RESEARCH* ]] || [[ "$file_name" == *research* ]]; then
        category="research"
    elif [[ "$file_name" == *MEETING* ]] || [[ "$file_name" == *meeting* ]]; then
        category="meetings"
    elif [[ "$file_name" == *LEARNING* ]] || [[ "$file_name" == *learning* ]] || [[ "$file_name" == *JOURNAL* ]] || [[ "$file_name" == *journal* ]]; then
        category="learning"
    elif [[ "$file_name" == *TECHNICAL* ]] || [[ "$file_name" == *technical* ]]; then
        category="technical-notes"
    elif [[ "$file_name" == *DEVELOPMENT* ]] || [[ "$file_name" == *development* ]]; then
        category="development"
    elif [[ "$file_name" == *REPORT* ]] || [[ "$file_name" == *report* ]]; then
        category="reports"
    else
        # Check content patterns
        if echo "$first_lines" | grep -q "implementation\|implementation\|technical\|architecture"; then
            category="implementation"
        elif echo "$first_lines" | grep -q "guide\|tutorial\|how to\|usage"; then
            category="user-guides"
        elif echo "$first_lines" | grep -q "api\|endpoint\|rest"; then
            category="api"
        elif echo "$first_lines" | grep -q "test\|testing\|spec"; then
            category="testing"
        elif echo "$first_lines" | grep -q "deploy\|deployment\|production"; then
            category="deployment"
        elif echo "$first_lines" | grep -q "research\|analysis\|investigation"; then
            category="research"
        elif echo "$first_lines" | grep -q "meeting\|minutes\|discussion"; then
            category="meetings"
        elif echo "$first_lines" | grep -q "learning\|journal\|reflection"; then
            category="learning"
        elif echo "$first_lines" | grep -q "technical\|notes\|details"; then
            category="technical-notes"
        elif echo "$first_lines" | grep -q "development\|coding\|programming"; then
            category="development"
        elif echo "$first_lines" | grep -q "report\|summary\|findings"; then
            category="reports"
        else
            # Default category based on directory
            if [[ "$dir_name" == *meeting* ]] || [[ "$dir_name" == *Meeting* ]]; then
                category="meetings"
            elif [[ "$dir_name" == *research* ]] || [[ "$dir_name" == *Research* ]]; then
                category="research"
            elif [[ "$dir_name" == *learning* ]] || [[ "$dir_name" == *Learning* ]]; then
                category="learning"
            else
                category="documentation"
            fi
        fi
    fi
    
    echo "$category"
}

# Find all markdown files recursively
echo "🔍 Searching for markdown files..."
md_files=($(find . -name "*.md" -type f 2>/dev/null | grep -v "node_modules" | grep -v ".git" | grep -v "venv" | grep -v "docs"))

echo "📊 Found ${#md_files[@]} markdown files"

# Process each file
processed_count=0
skipped_count=0

for file_path in "${md_files[@]}"; do
    echo "Processing: $file_path"
    
    # Skip if file doesn't exist
    if [ ! -f "$file_path" ]; then
        echo "  ⏭️  Skipped: File not found"
        ((skipped_count++))
        continue
    fi
    
    # Determine category
    category=$(categorize_file "$file_path")
    
    # Create target directory
    target_dir="docs/$category"
    mkdir -p "$target_dir"
    
    # Generate target filename
    file_name=$(basename "$file_path")
    target_file="$target_dir/$file_name"
    
    # Handle duplicate filenames
    counter=1
    original_target="$target_file"
    while [ -f "$target_file" ]; do
        name_without_ext="${file_name%.*}"
        ext="${file_name##*.}"
        target_file="$target_dir/${name_without_ext}_${counter}.${ext}"
        ((counter++))
    done
    
    # Copy file
    cp "$file_path" "$target_file"
    echo "  ✅ Copied to: $target_file"
    ((processed_count++))
done

echo ""
echo "📋 Processing Summary:"
echo "====================="
echo "📊 Total files found: ${#md_files[@]}"
echo "✅ Files processed: $processed_count"
echo "⏭️  Files skipped: $skipped_count"

# Create comprehensive documentation index
echo ""
echo "📋 Creating comprehensive documentation index..."

cat > docs/README.md << 'EOF'
# 📚 Project Documentation Hub

This directory contains all project documentation organized by category.

## 📁 Directory Structure

### 📄 Documentation
Core project documentation and overview files.
- `documentation/` - Main project documentation
- `user-guides/` - User guides and tutorials
- `guides/` - Getting started and how-to guides

### 🔧 Technical Documentation
Implementation and technical details.
- `implementation/` - Feature implementation details
- `architecture/` - System architecture documentation
- `api/` - API documentation and specifications
- `development/` - Development guidelines and practices
- `technical-notes/` - Technical notes and details

### 📊 Reports & Research
Research findings and analysis.
- `reports/` - Research reports and analysis
- `research/` - Research materials and findings

### 📅 Project Management
Project management and collaboration.
- `meetings/` - Meeting notes and minutes
- `learning/` - Learning journals and reflections

### 🧪 Quality Assurance
Testing and deployment documentation.
- `testing/` - Testing documentation and specifications
- `deployment/` - Deployment and production guides

## 🚀 Quick Navigation

### Getting Started
- **Quick Start**: [Quick Start Guide](guides/QUICK_START.md)
- **How to Start**: [How to Start](guides/HOW_TO_START.md)
- **User Guides**: [User Guides](user-guides/)

### Technical Information
- **Architecture**: [Technical Architecture](architecture/TECHNICAL_ARCHITECTURE.md)
- **Implementation**: [Implementation Details](implementation/)
- **API**: [API Documentation](api/)

### Project Management
- **Meetings**: [Meeting Notes](meetings/)
- **Research**: [Research Materials](research/)
- **Reports**: [Research Reports](reports/)

## 📝 Contributing

When adding new documentation:
1. Choose the appropriate category
2. Follow the existing naming conventions
3. Update this index if needed
4. Use clear, descriptive titles

### Category Guidelines

- **documentation/**: Core project overview and essential information
- **user-guides/**: Step-by-step user instructions and tutorials
- **guides/**: Getting started and setup instructions
- **implementation/**: Technical implementation details
- **architecture/**: System design and architecture
- **api/**: API specifications and documentation
- **development/**: Development practices and guidelines
- **technical-notes/**: Technical details and notes
- **reports/**: Research reports and analysis
- **research/**: Research materials and findings
- **meetings/**: Meeting notes and minutes
- **learning/**: Learning journals and reflections
- **testing/**: Testing documentation and specifications
- **deployment/**: Deployment and production guides

---

*Last updated: $(date)*
EOF

echo "✅ Created comprehensive docs/README.md"

# Create category-specific README files
echo "📝 Creating category README files..."

# Documentation README
cat > docs/documentation/README.md << 'EOF'
# 📄 Documentation

This directory contains core project documentation and overview files.

## Purpose

Core documentation provides essential information about the project, its purpose, and how to get started.

## Files

This directory contains README files and other core documentation.
EOF

# User Guides README
cat > docs/user-guides/README.md << 'EOF'
# 📖 User Guides

This directory contains user guides and tutorials.

## Purpose

User guides provide step-by-step instructions for using the application features.

## Files

This directory contains usage guides and tutorials for various features.
EOF

# Guides README
cat > docs/guides/README.md << 'EOF'
# 🚀 Guides

This directory contains getting started guides and setup instructions.

## Purpose

Guides provide instructions for setting up and getting started with the project.

## Files

This directory contains quick start guides and setup instructions.
EOF

# Implementation README
cat > docs/implementation/README.md << 'EOF'
# 🔧 Implementation

This directory contains implementation details and technical documentation.

## Purpose

Implementation documentation provides detailed technical information about how features are built.

## Files

This directory contains implementation summaries and technical details.
EOF

# Architecture README
cat > docs/architecture/README.md << 'EOF'
# 🏗️ Architecture

This directory contains system architecture documentation.

## Purpose

Architecture documentation describes the system design and structure.

## Files

This directory contains technical architecture and design documents.
EOF

# API README
cat > docs/api/README.md << 'EOF'
# 🔌 API Documentation

This directory contains API documentation and specifications.

## Purpose

API documentation provides information about endpoints, requests, and responses.

## Files

This directory contains API specifications and documentation.
EOF

# Development README
cat > docs/development/README.md << 'EOF'
# 💻 Development

This directory contains development guidelines and practices.

## Purpose

Development documentation provides guidelines for coding and development practices.

## Files

This directory contains development guidelines and best practices.
EOF

# Technical Notes README
cat > docs/technical-notes/README.md << 'EOF'
# 📝 Technical Notes

This directory contains technical notes and details.

## Purpose

Technical notes provide detailed technical information and implementation notes.

## Files

This directory contains technical details and implementation notes.
EOF

# Reports README
cat > docs/reports/README.md << 'EOF'
# 📊 Reports

This directory contains research reports and analysis documents.

## Purpose

Reports contain research findings, analysis, and technical investigations.

## Files

This directory contains research reports and analysis documents.
EOF

# Research README
cat > docs/research/README.md << 'EOF'
# 🔬 Research

This directory contains research materials and findings.

## Purpose

Research materials provide background information and investigation results.

## Files

This directory contains research materials and findings.
EOF

# Meetings README
cat > docs/meetings/README.md << 'EOF'
# 📅 Meetings

This directory contains meeting notes and minutes.

## Purpose

Meeting notes document discussions, decisions, and action items from project meetings.

## Files

This directory contains meeting notes and minutes.
EOF

# Learning README
cat > docs/learning/README.md << 'EOF'
# 🎓 Learning

This directory contains learning journals and reflections.

## Purpose

Learning journals document team learning experiences and reflections.

## Files

This directory contains learning journals and reflection documents.
EOF

# Testing README
cat > docs/testing/README.md << 'EOF'
# 🧪 Testing

This directory contains testing documentation and specifications.

## Purpose

Testing documentation provides information about testing strategies and specifications.

## Files

This directory contains testing documentation and test specifications.
EOF

# Deployment README
cat > docs/deployment/README.md << 'EOF'
# 🚀 Deployment

This directory contains deployment and production guides.

## Purpose

Deployment documentation provides information about deploying and maintaining the application.

## Files

This directory contains deployment guides and production documentation.
EOF

echo "✅ Created category README files"

# Show final structure
echo ""
echo "📁 Final Documentation Structure:"
echo "================================"
find docs -type f -name "*.md" | sort

echo ""
echo "🎉 Comprehensive document organization complete!"
echo ""
echo "📋 Summary:"
echo "   - Created comprehensive directory structure"
echo "   - Processed $processed_count markdown files"
echo "   - Organized files into 14 categories"
echo "   - Created index and category README files"
echo ""
echo "💡 Next steps:"
echo "   1. Review the organized structure in docs/"
echo "   2. Update any internal links if needed"
echo "   3. Consider removing original files after verification"
echo "   4. Update .gitignore if needed"

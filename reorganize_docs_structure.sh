#!/bin/bash

echo "🔄 Reorganizing Documentation Structure"
echo "======================================"

# Define folders to preserve
preserved_folders=(
    "docs/learning"
    "docs/LearningJournal"
    "docs/meetings"
    "docs/presentations"
    "docs/preview"
    "docs/reports"
    "docs/videos"
)

echo "📁 Preserving specified folders:"
for folder in "${preserved_folders[@]}"; do
    echo "   - $folder"
done

# Create new category folders
new_categories=(
    "docs/documentation"
    "docs/guides"
    "docs/implementation"
    "docs/architecture"
    "docs/api"
    "docs/testing"
    "docs/development"
    "docs/deployment"
    "docs/user-guides"
    "docs/technical-notes"
    "docs/research"
)

echo ""
echo "📂 Creating new category structure..."

# Create new category directories
for category in "${new_categories[@]}"; do
    mkdir -p "$category"
    echo "   ✅ Created: $category"
done

# Function to categorize file
categorize_file() {
    local file_path="$1"
    local file_name=$(basename "$file_path")
    local dir_name=$(dirname "$file_path")
    
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
    elif [[ "$file_name" == *TECHNICAL* ]] || [[ "$file_name" == *technical* ]]; then
        category="technical-notes"
    elif [[ "$file_name" == *DEVELOPMENT* ]] || [[ "$file_name" == *development* ]]; then
        category="development"
    elif [[ "$file_name" == *REPORT* ]] || [[ "$file_name" == *report* ]]; then
        category="reports"
    else
        # Read first few lines to determine category
        local first_lines=$(head -10 "$file_path" 2>/dev/null | tr '[:upper:]' '[:lower:]')
        
        # Check content patterns
        if echo "$first_lines" | grep -q "implementation\|technical\|architecture"; then
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
        elif echo "$first_lines" | grep -q "technical\|notes\|details"; then
            category="technical-notes"
        elif echo "$first_lines" | grep -q "development\|coding\|programming"; then
            category="development"
        elif echo "$first_lines" | grep -q "report\|summary\|findings"; then
            category="reports"
        else
            # Default category
            category="documentation"
        fi
    fi
    
    echo "$category"
}

# Find all markdown files in docs (excluding preserved folders)
echo ""
echo "🔍 Finding files to reorganize..."

# Create a temporary list of files to process
temp_file_list=$(mktemp)

# Find all markdown files in docs, excluding preserved folders
find docs -name "*.md" -type f | while read -r file; do
    # Check if file is in a preserved folder
    should_preserve=false
    for folder in "${preserved_folders[@]}"; do
        if [[ "$file" == "$folder"* ]]; then
            should_preserve=true
            break
        fi
    done
    
    # If not in preserved folder, add to processing list
    if [ "$should_preserve" = false ]; then
        echo "$file" >> "$temp_file_list"
    fi
done

# Read the file list and process
if [ -f "$temp_file_list" ]; then
    files_to_process=($(cat "$temp_file_list"))
    rm "$temp_file_list"
else
    files_to_process=()
fi

echo "📊 Found ${#files_to_process[@]} files to reorganize"

# Process each file
processed_count=0
skipped_count=0

for file_path in "${files_to_process[@]}"; do
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
    
    # Move file
    mv "$file_path" "$target_file"
    echo "  ✅ Moved to: $target_file"
    ((processed_count++))
done

echo ""
echo "📋 Processing Summary:"
echo "====================="
echo "📊 Total files found: ${#files_to_process[@]}"
echo "✅ Files moved: $processed_count"
echo "⏭️  Files skipped: $skipped_count"

# Show final structure
echo ""
echo "📁 Final Documentation Structure:"
echo "================================"

# Show preserved folders
echo "📁 Preserved Folders:"
for folder in "${preserved_folders[@]}"; do
    if [ -d "$folder" ]; then
        file_count=$(find "$folder" -name "*.md" -type f 2>/dev/null | wc -l)
        echo "   - $folder ($file_count files)"
    fi
done

echo ""
echo "📁 Reorganized Categories:"
for category in "${new_categories[@]}"; do
    if [ -d "$category" ]; then
        file_count=$(find "$category" -name "*.md" -type f 2>/dev/null | wc -l)
        echo "   - $category ($file_count files)"
    fi
done

# Update main README
echo ""
echo "📝 Updating main README..."

cat > docs/README.md << 'EOF'
# 📚 Project Documentation Hub

This directory contains all project documentation organized by category.

## 📁 Directory Structure

### 📅 Preserved Folders
- `learning/` - Learning materials and resources
- `LearningJournal/` - Team learning journals
- `meetings/` - Meeting notes and minutes
- `presentations/` - Project presentations
- `preview/` - Preview materials and demos
- `reports/` - Research reports and analysis
- `videos/` - Project videos and demos

### 📄 Documentation Categories
- `documentation/` - Core project documentation and README files
- `guides/` - Getting started and setup guides
- `user-guides/` - User guides and tutorials
- `implementation/` - Technical implementation details
- `architecture/` - System architecture documentation
- `api/` - API documentation and specifications
- `testing/` - Testing documentation and specifications
- `development/` - Development guidelines and practices
- `deployment/` - Deployment and production guides
- `technical-notes/` - Technical notes and details
- `research/` - Research materials and findings

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
- **guides/**: Getting started and setup instructions
- **user-guides/**: Step-by-step user instructions and tutorials
- **implementation/**: Technical implementation details
- **architecture/**: System design and architecture
- **api/**: API specifications and documentation
- **testing/**: Testing documentation and specifications
- **development/**: Development practices and guidelines
- **deployment/**: Deployment and production guides
- **technical-notes/**: Technical details and notes
- **research/**: Research materials and findings

---

*Last updated: $(date)*
EOF

echo "✅ Updated docs/README.md"

echo ""
echo "🎉 Reorganization complete!"
echo ""
echo "📋 Summary:"
echo "   - Preserved specified folders"
echo "   - Reorganized $processed_count files into new categories"
echo "   - Updated main documentation index"
echo ""
echo "💡 Next steps:"
echo "   1. Review the new structure in docs/"
echo "   2. Update any internal links if needed"
echo "   3. Consider cleaning up duplicate files"


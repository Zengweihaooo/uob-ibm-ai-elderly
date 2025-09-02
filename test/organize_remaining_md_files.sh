#!/bin/bash

echo "📚 Organizing Remaining Markdown Files"
echo "====================================="

# Create docs subdirectories if they don't exist
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

echo "📁 Ensuring directory structure exists..."

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
            elif [[ "$dir_name" == *springboot* ]]; then
                category="implementation"
            elif [[ "$dir_name" == *commit-logs* ]]; then
                category="development"
            elif [[ "$dir_name" == *memoir_md* ]]; then
                category="documentation"
            elif [[ "$dir_name" == *release-notes* ]]; then
                category="documentation"
            else
                category="documentation"
            fi
        fi
    fi
    
    echo "$category"
}

# Find all remaining markdown files
echo "🔍 Searching for remaining markdown files..."
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

# Show final structure
echo ""
echo "📁 Final Documentation Structure:"
echo "================================"
find docs -type f -name "*.md" | wc -l | xargs echo "Total markdown files in docs:"

echo ""
echo "🎉 Remaining files organization complete!"
echo ""
echo "📋 Summary:"
echo "   - Processed $processed_count additional markdown files"
echo "   - Organized files into appropriate categories"
echo "   - Maintained existing organized structure"
echo ""
echo "💡 Next steps:"
echo "   1. Review the updated structure in docs/"
echo "   2. Consider removing original files from root directory"
echo "   3. Update any internal links if needed"


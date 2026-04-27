import { processFiles } from './processFiles.js';
import { exec, execSync, spawnSync } from 'child_process';
import path from 'path';
import { fileURLToPath } from 'url';
import { readdir } from 'fs/promises';
import { readFileSync } from 'fs';
import { join } from 'path';

// ------ CONFIGURATION: Load from config file ----------
const configPath = process.argv[2];
if (!configPath) {
  console.error('Error: Please provide a config file path.');
  console.error('Usage: node snip.js <config-file-path>');
  console.error('Example: node snip.js tanstack/snip.config.json');
  process.exit(1);
}

let config;
try {
  const configContent = readFileSync(configPath, 'utf-8');
  config = JSON.parse(configContent);
} catch (error) {
  console.error(`Error reading config file: ${configPath}`);
  console.error(error.message);
  process.exit(1);
}

const IGNORE_PATTERNS = new Set(config.IGNORE_PATTERNS || []);
const START_DIRECTORY = config.START_DIRECTORY;
const OUTPUT_DIRECTORY = config.OUTPUT_DIRECTORY;
const PRETTIER_CONFIG = config.PRETTIER_CONFIG || '.prettierrc';
// ------ END CONFIGURATION --------------------------------------------------

// Resolve paths relative to the config file's directory
const CONFIG_DIRECTORY = path.dirname(path.resolve(configPath));
const SCRIPT_DIRECTORY = path.dirname(fileURLToPath(import.meta.url));
const PRETTIER_CONFIG_PATH = path.resolve(CONFIG_DIRECTORY, PRETTIER_CONFIG);

// Validate required config
if (!START_DIRECTORY || !OUTPUT_DIRECTORY) {
  console.error('Error: Config file must contain START_DIRECTORY and OUTPUT_DIRECTORY');
  process.exit(1);
}

// Check if Bluehawk is installed
function isBluehawkInstalled() {
  const errorString =
    'This script requires Bluehawk. Please run "npm install -g bluehawk" in the terminal, and then re-run this script.';

  const result = spawnSync('which', ['bluehawk'], { encoding: 'utf-8' });

  // If the spawnSync operation returns an exit code of 1, there was an error
  // running 'which bluehawk' and we can assume Bluehawk isn't installed
  if (result.status == 1) {
    console.error(errorString);
    return false;
  }
  return true;
}

// Resolves relative paths to absolute paths based on the Git repository root.
function resolvePathFromGitRoot(relativePath) {
  let gitRoot;
  try {
    gitRoot = execSync('git rev-parse --show-toplevel', {
      encoding: 'utf8',
    }).trim();
  } catch (error) {
    console.error(
      'Error: Unable to determine the Git repository root. Ensure this script is run within a Git repository.'
    );
    throw error;
  }
  return path.resolve(gitRoot, relativePath);
}

// Check if Prettier is installed
function isPrettierInstalled() {
  try {
    execSync('prettier --version', { stdio: 'ignore' }); // Check Prettier availability
    return true;
  } catch {
    console.log('Prettier is not installed. Skipping formatting step...');
    return false;
  }
}

// Helper to recursively get all JavaScript/TypeScript files in a directory
async function getAllJsFiles(dir, fileList = []) {
  const entries = await readdir(dir, { withFileTypes: true });

  for (const entry of entries) {
    const fullPath = join(dir, entry.name);
    if (entry.isDirectory()) {
      await getAllJsFiles(fullPath, fileList);
    } else if (entry.isFile() && /\.(js|jsx|ts|tsx)$/.test(entry.name)) {
      fileList.push(fullPath);
    }
  }

  return fileList;
}

// Helper to format a single file with prettier
function formatFile(filePath) {
  return new Promise((resolve) => {
    const command = `prettier --config "${PRETTIER_CONFIG_PATH}" --write "${filePath}"`;
    exec(command, (error, stdout, stderr) => {
      if (error) {
        // File couldn't be formatted (likely syntax error), skip it
        resolve({ success: false, file: filePath, error: error.message });
      } else {
        resolve({ success: true, file: filePath });
      }
    });
  });
}

// Helper to run the formatting tool on the output directory
async function runFormatter(directory) {
  try {
    // Get all JS/TS files in the directory
    const jsFiles = await getAllJsFiles(directory);

    if (jsFiles.length === 0) {
      console.log('No JavaScript/TypeScript files found to format.');
      return;
    }

    console.log(`Found ${jsFiles.length} JavaScript/TypeScript file(s) to format...`);

    // Format each file individually
    const results = await Promise.all(jsFiles.map(formatFile));

    // Summarize results
    const successful = results.filter((r) => r.success);
    const failed = results.filter((r) => !r.success);

    console.log(`Successfully formatted ${successful.length} file(s).`);

    if (failed.length > 0) {
      console.log(
        `\nSkipped ${failed.length} file(s) that could not be formatted:`
      );
      failed.forEach(({ file }) => {
        const relativePath = path.relative(directory, file);
        console.log(`  - ${relativePath}`);
      });
    }
  } catch (error) {
    console.error('Error during formatting:', error.message);
  }
}

// Snip code example files, and then run the formatting tool on the output
async function main() {
  // First, confirm the user has Bluehawk installed. If not, exit early.
  const bluehawkInstalled = isBluehawkInstalled();

  if (!bluehawkInstalled) {
    process.exit(1);
  }

  // If the user does have Bluehawk installed, process the code example files.
  try {
    // Resolve paths relative to the config file directory
    const resolvedStartDir = path.resolve(CONFIG_DIRECTORY, START_DIRECTORY);
    const resolvedOutputDir = path.resolve(CONFIG_DIRECTORY, OUTPUT_DIRECTORY);

    console.log(`Processing example files in ${resolvedStartDir}`);
    console.log(`Output directory: ${resolvedOutputDir}`);

    // Snip the code example files to the output directory
    await processFiles(resolvedStartDir, resolvedOutputDir, IGNORE_PATTERNS);

    // If the person running the script has Prettier installed, use it to run the
    // formatting script on the resolved output directory.
    const prettierInstalled = isPrettierInstalled();
    if (prettierInstalled) {
      console.log(
        `Processing Completed.\nRunning formatter on output directory: ${resolvedOutputDir}`
      );
      await runFormatter(resolvedOutputDir);
      console.log('Formatting completed.');
    } else {
      // If the user does not have Prettier installed, snip files directly to
      // the output directory without formatting them.
      console.log(
        `Files processed but not formatted due to missing formatting dependency.`
      );
    }
  } catch (error) {
    console.error('Error during processing or formatting:', error);
  }
}

main();
